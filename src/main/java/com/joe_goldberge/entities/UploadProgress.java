package com.joe_goldberge.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Document(collection = "upload_progress")
public class UploadProgress {
    @Id
    private String jobId;
    private int totalPages;
    private int currentPage;
    private int batchSize;
    private int totalFilesProcessed;
    private int totalFilesSuccessful;
    private int totalFilesFailed;
    private Set<String> processedFileUrls = new HashSet<>();
    private Set<String> failedFileUrls = new HashSet<>();
    private String status = "RUNNING";
    private Date startedAt = new Date();
    private Date lastUpdatedAt = new Date();
}

