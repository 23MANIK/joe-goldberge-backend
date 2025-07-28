package com.joe_goldberge.service;

import com.joe_goldberge.entities.MongoUserDTO;
import com.joe_goldberge.entities.UploadProgress;
import com.joe_goldberge.entities.UserContent;
import com.joe_goldberge.entities.UserProfile;
import com.joe_goldberge.model.UploadTask;
import com.joe_goldberge.repository.UploadProgressRepository;
import com.joe_goldberge.utility.FileNameUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component
@Slf4j
public class TeraBoxBatchUploadService{

    private static final int BATCH_SIZE = 10;             // Users per batch
    private static final int THREAD_POOL_SIZE = 10;        // Parallel uploads per batch
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int UPLOAD_TIMEOUT_MINUTES = 30;

    @Autowired
    DataReadService dataReadService;

    @Autowired
    private UploadProgressRepository progressRepository;


    public synchronized String startBatchUpload() {
        log.info("Starting batch upload process...");
        long userCount = dataReadService.getCount();
        log.info("Total user count: {}", userCount);
        int totalPages = (int) Math.ceil((double) userCount / BATCH_SIZE);
        log.info("Total pages to process: {} (Batch size: {})", totalPages, BATCH_SIZE);
        String jobId = "upload_job_" + System.currentTimeMillis();
        log.info("Generated jobId: {}", jobId);
        UploadProgress progress = new UploadProgress();
        progress.setJobId(jobId);
        progress.setTotalPages(totalPages);
        progress.setCurrentPage(0);
        progress.setBatchSize(BATCH_SIZE);
        progress.setStatus("RUNNING");
        progressRepository.save(progress);
        log.info("UploadProgress entity saved for jobId: {}", jobId);
        processBatchUpload(progress);
        log.info("Started batch upload with jobId {}", jobId);
        return jobId;
    }

    @Async("uploadTaskExecutor")
    public void processBatchUpload(UploadProgress progress) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try {
            for (int page = progress.getCurrentPage(); page < progress.getTotalPages(); page++) {
                progress.setCurrentPage(page);
//                Page<MongoUserDTO> userPage = userRepository.findAll(PageRequest.of(page, BATCH_SIZE));
                Page<MongoUserDTO> userPage = dataReadService.getAllUsers(PageRequest.of(page, BATCH_SIZE));


                System.out.println("Processing page " + (page + 1) + " of " + progress.getTotalPages());
                List<MongoUserDTO> userList = userPage.getContent();
                if (CollectionUtils.isEmpty(userList)) break;
                Map<String, String> userIdToName = userList.stream()
                        .collect(Collectors.toMap(MongoUserDTO::getUserId, u -> {
                            UserProfile profile = u.getUserProfile();
                            return (profile != null && profile.getFirstName() != null) ? profile.getFirstName() : "Unknown";
                        }, (a, b) -> a));
                List<String> userIds = userList.stream().map(MongoUserDTO::getUserId).collect(Collectors.toList());

                List<UserContent> contents = dataReadService.getAllContents(userIds);

                List<UploadTask> uploadTasks = new ArrayList<>();
                for (UserContent uc : contents) {
                    String userId = uc.getUserId();
                    String firstName = userIdToName.getOrDefault(userId, "Unknown");
                    if (uc.getContent() != null && uc.getContent().getPhotos() != null) {
                        for (UserContent.Content.Photo photo : uc.getContent().getPhotos()) {
                            String url = photo.getVideoUrl();
                            if (url == null || url.isEmpty()) continue;
                            String oldFileName = FileNameUtil.extractFileNameFromUrl(url);
                            String newFileName = FileNameUtil.buildNewFileName(userId, firstName, oldFileName);
                            if (progress.getProcessedFileUrls().contains(url)) continue;
                            uploadTasks.add(new UploadTask(url, newFileName, userId, firstName));
                        }
                    }
                }
                List<Future<Boolean>> results = new ArrayList<>();
                for (UploadTask task : uploadTasks) {
                    results.add(executor.submit(() -> processUploadTask(task, progress)));
                }
                for (int i = 0; i < uploadTasks.size(); i++) {
                    UploadTask task = uploadTasks.get(i);
                    try {
                        boolean success = results.get(i).get(UPLOAD_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                        if (success) {
                            progress.getProcessedFileUrls().add(task.getUrl());
                            progress.setTotalFilesSuccessful(progress.getTotalFilesSuccessful() + 1);
                        } else {
                            progress.getFailedFileUrls().add(task.getUrl());
                            progress.setTotalFilesFailed(progress.getTotalFilesFailed() + 1);
                        }
                        progress.setTotalFilesProcessed(progress.getTotalFilesProcessed() + 1);
                    } catch (Exception ex) {
                        log.error("Failed to upload file {} after retries - {}", task.getFileName(), ex.getMessage());
                        progress.getFailedFileUrls().add(task.getUrl());
                        progress.setTotalFilesFailed(progress.getTotalFilesFailed() + 1);
                    }
                }
                progress.setLastUpdatedAt(new Date());
                progressRepository.save(progress);
                if (!"RUNNING".equals(progress.getStatus())) {
                    log.warn("Batch upload (jobId {}) interrupted at page {}", progress.getJobId(), page);
                    break;
                }
            }
            progress.setStatus("COMPLETED");
        } catch (Exception ex) {
            log.error("Batch upload failed: {}", ex.getMessage(), ex);
            progress.setStatus("FAILED");
        } finally {
            progress.setLastUpdatedAt(new Date());
            progressRepository.save(progress);
            executor.shutdown();
        }
    }

    private boolean processUploadTask(UploadTask task, UploadProgress progress) {
        int attempt = 0;
        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                uploadFileByLink(task.getUrl(), task.getFileName());
                log.info("Successfully uploaded: {}", task.getFileName());
                return true;
            } catch (Exception ex) {
                attempt++;
                log.warn("Upload attempt {} failed for {}: {}", attempt, task.getFileName(), ex.getMessage());
                try {
                    Thread.sleep((long) Math.pow(2, attempt) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        log.error("Giving up on {}, all {} attempts failed.", task.getFileName(), MAX_RETRY_ATTEMPTS);
        return false;
    }

    private void uploadFileByLink(String url, String newFileName) throws IOException, InterruptedException {
        // Adjust the path to your Node.js script accordingly
        String nodeScriptPath = "src/node-scripts/upload.js";

        // Build the command: node your_node_script.js <url> <newFileName>
        String[] command = {"node", nodeScriptPath, url, newFileName};

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);  // Redirect error stream to standard output

        Process process = pb.start();

        // Read output from the Node.js process
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Node.js] " + line);
            }
        }

        int exitCode = process.waitFor();
        System.out.println("Node.js process exited with code: " + exitCode);

        if (exitCode != 0) {
            throw new IOException("Node.js script exited with non-zero code: " + exitCode);
        }
    }

}
