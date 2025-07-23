package com.joe_goldberge.service;

import com.mongodb.client.*;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;

public class UserDetailsService {

    private static final String BEARER_AUTH_TOKEN = System.getenv("BEARER_AUTH_TOKEN");
    private static final String SESSION_ID = System.getenv("SESSION_ID");
    private final MongoCollection<Document> collection;

    public UserDetailsService(MongoClient mongoClient, String dbName, String collectionName) {
        this.collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
    }

    // --- 1. Build API URL for userId batch ---
    private String buildRequestUrl(List<String> userIds) {
        String idsParam = String.join(",", userIds);
        return "https://prod-api.hingeaws.net/user/v3/public?ids=" + idsParam;
    }

    // --- 2. Prepare HTTP Connection and Set Headers ---
    private HttpURLConnection buildConnection(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setRequestMethod("GET");

        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "en-GB");
        conn.setRequestProperty("Authorization", BEARER_AUTH_TOKEN);
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Host", "prod-api.hingeaws.net");
        conn.setRequestProperty("User-Agent", "Hinge/11615 CFNetwork/3826.500.131 Darwin/24.5.0");
        conn.setRequestProperty("X-App-Version", "9.81.0");
        conn.setRequestProperty("X-Build-Number", "11615");
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

    // --- 3. Fetch User Data as String (JSON array) ---
    private String fetchUserData(List<String> userIds) throws IOException {
        String urlStr = buildRequestUrl(userIds);
        HttpURLConnection conn = buildConnection(urlStr);
        int status = conn.getResponseCode();

        if (status != 200) {
            throw new IOException("HTTP error: " + status);
        }
        StringBuilder content = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
        } finally {
            conn.disconnect();
        }
        return content.toString();
    }

    // --- 4. Parse and Save Each User Profile ---
    private void saveProfilesToDb(String userJsonArray) {
        JSONArray userArray = new JSONArray(userJsonArray);
        for (int i = 0; i < userArray.length(); i++) {
            JSONObject userObj = userArray.getJSONObject(i);
            Document doc = Document.parse(userObj.toString());
            String userId = userObj.optString("userId", null);

            if (userId != null) {
                collection.replaceOne(
                        new Document("userId", userId), doc,
                        new ReplaceOptions().upsert(true)
                );
            } else {
                collection.insertOne(doc);
            }
        }
        System.out.println("✅ Saved " + userArray.length() + " profiles to MongoDB.");
    }

    // --- 5. Orchestration: Fetch and Store in One Call ---
    public void fetchAndSaveUsers(List<String> userIds) {
        if (userIds.isEmpty()) return;
        try {
            String userJsonArray = fetchUserData(userIds);
            saveProfilesToDb(userJsonArray);
        } catch (Exception e) {
            System.out.println("❌ Error fetching/saving users: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
