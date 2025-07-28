package com.joe_goldberge.web;

import com.joe_goldberge.entities.MongoUserDTO;

import com.joe_goldberge.entities.UserContent;
import com.joe_goldberge.repository.UserContentRepository;
import com.joe_goldberge.repository.UsersRepository;
import com.joe_goldberge.service.DataReadService;
import com.joe_goldberge.service.RecordFetcher;
import com.joe_goldberge.service.TeraBoxBatchUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@CrossOrigin(origins = {"https://joe-goldberg.vercel.app", "http://localhost:3000"})
@RestController
@RequestMapping("/api")
@Slf4j
public class UserController {

    @Autowired
    DataReadService service;

    @Autowired
    private TeraBoxBatchUploadService teraBoxBatchUploadService;


    @GetMapping("/users")
    public PagedModel<EntityModel<MongoUserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String hometown,
            PagedResourcesAssembler<MongoUserDTO> assembler
    ) {
        Page<MongoUserDTO> paged = service.getAllUsers(page, size, firstName, education, location, hometown);
        return assembler.toModel(paged);
    }

    @GetMapping("/content")
    public List<UserContent> getUserContents(@RequestParam String userIds) {
        System.out.println(userIds);;
        List<String> userIdList = Arrays.asList(userIds.split(","));
        return service.getAllContents(userIdList);
    }

    @GetMapping("/loadUsers")
    public ResponseEntity<Map<String, Object>> loadUsers() {

        RecordFetcher fetcher = new RecordFetcher();
        fetcher.loadNewUsers();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Users loaded successfully.");

        return ResponseEntity.ok(response);

    }


    @GetMapping("/mongo-health")
    public String testMongo() {
        long count = service.getCount();
        return "User count: " + count;
    }


    @PostMapping("/batch/start")
    public ResponseEntity<Map<String, String>> startBatchUpload() {
        try {
            log.info("Received request to start batch upload");
            String jobId = teraBoxBatchUploadService.startBatchUpload();
            Map<String, String> response = new HashMap<>();
            response.put("status", "STARTED");
            response.put("message", "Batch upload process has been started");
            response.put("jobId", jobId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.info("Failed to start batch upload", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Failed to start batch upload: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
