package com.joe_goldberge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class RecordFetcher {

    private static final String BEARER_AUTH_TOKEN = System.getenv("BEARER_AUTH_TOKEN");
    private static final String SESSION_ID = System.getenv("SESSION_ID");
    // --- 1. Build API URL for userId batch ---
    private HttpURLConnection buildPostConnection(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "en-GB");
        conn.setRequestProperty("Authorization", BEARER_AUTH_TOKEN);
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Host", "prod-api.hingeaws.net");
        conn.setRequestProperty("User-Agent", "Hinge/11616 CFNetwork/3826.500.131 Darwin/24.5.0");
        conn.setRequestProperty("X-App-Version", "9.82.0");
        conn.setRequestProperty("X-Build-Number", "11616");
        conn.setRequestProperty("X-Device-Id", "491DB5EB-2C78-4774-9130-FB384A764D2F");
        conn.setRequestProperty("X-Device-Model", "unknown");
        conn.setRequestProperty("X-Device-Model-Code", "iPhone14,5");
        conn.setRequestProperty("X-Device-Platform", "iOS");
        conn.setRequestProperty("X-Device-Region", "IN");
        conn.setRequestProperty("X-Install-Id", "327E3786-75CB-49FB-A323-18DD0B343E44");
        conn.setRequestProperty("X-OS-Version", "18.5");
        conn.setRequestProperty("X-Session-Id", SESSION_ID);

        return conn;
    }

    // --- Send the POST request and parse subject IDs ---
    public List<String> fetchSubjectIds() throws IOException {
        String endpoint = "https://prod-api.hingeaws.net/rec/v2";
        String payload = "{ \"newHere\": true, \"playerId\": \"3292315947120985319\", \"activeToday\": true }";
        HttpURLConnection conn = buildPostConnection(endpoint);

        // Write body
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
        }

        // Handle response
        InputStream is = conn.getResponseCode() < 400
                ? conn.getInputStream()
                : conn.getErrorStream();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(is);

        // Traverse feeds -> subjects -> subjectId
        List<String> subjectIds = new ArrayList<>();
        JsonNode feeds = root.get("feeds");
        if (feeds != null && feeds.isArray()) {
            for (JsonNode feed : feeds) {
                JsonNode subjects = feed.get("subjects");
                if (subjects != null && subjects.isArray()) {
                    for (JsonNode subject : subjects) {
                        JsonNode idNode = subject.get("subjectId");
                        if (idNode != null) {
                            subjectIds.add(idNode.asText());
                        }
                    }
                }
            }
        }
        conn.disconnect();
        return subjectIds;
    }

    public void loadNewUsers() {
        try {
            List<String> userIds = fetchSubjectIds();
            // Here you would typically save these IDs to your database or process them further
            System.out.println("Fetched Subject IDs: " + userIds);

            String uri = System.getenv("MONGODB_ATLAS_URI");
            if (uri == null || uri.isBlank()) {
                throw new IllegalStateException("MongoDB Atlas URI is not set. Set MONGODB_ATLAS_URI in your environment.");
            }

            try (MongoClient mongoClient = MongoClients.create(uri)) {
                UserDetailsService profileService = new UserDetailsService(mongoClient, "hingeDB", "users");
                UserContentService contentService = new UserContentService(mongoClient, "hingeDB", "user_content");
                profileService.fetchAndSaveUsers(userIds);
                contentService.fetchAndSaveContent(userIds);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
