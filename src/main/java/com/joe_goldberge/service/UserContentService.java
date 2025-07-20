package com.joe_goldberge.service;

import com.mongodb.client.*;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;

public class UserContentService {

    private static final String BEARER_AUTH_TOKEN = System.getenv("BEARER_AUTH_TOKEN");
    private static final String SESSION_ID = System.getenv("SESSION_ID");
    private final MongoCollection<Document> contentCollection;

    public UserContentService(MongoClient mongoClient, String dbName, String contentCollectionName) {
        this.contentCollection = mongoClient.getDatabase(dbName).getCollection(contentCollectionName);
    }

    private String buildContentRequestUrl(List<String> userIds) {
        return "https://prod-api.hingeaws.net/content/v2/public?ids=" + String.join(",", userIds);
    }

    private HttpURLConnection buildContentConnection(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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

    private String fetchContent(List<String> userIds) throws IOException {
        String urlStr = buildContentRequestUrl(userIds);
        HttpURLConnection conn = buildContentConnection(urlStr);
        int status = conn.getResponseCode();
        if (status != 200) throw new IOException("HTTP error: " + status);

        StringBuilder content = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) content.append(inputLine);
        } finally {
            conn.disconnect();
        }
        return content.toString();
    }

    public void fetchAndSaveContent(List<String> userIds) {
        if (userIds.isEmpty()) return;
        try {
            String jsonData = fetchContent(userIds);
            JSONArray array = new JSONArray(jsonData);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String userId = obj.optString("userId", null);
                Document doc = Document.parse(obj.toString());
                if (userId != null) {
                    contentCollection.replaceOne(new Document("userId", userId), doc, new ReplaceOptions().upsert(true));
                } else {
                    contentCollection.insertOne(doc);
                }
            }
            System.out.println("✅ Saved " + array.length() + " content records.");
        } catch (Exception e) {
            System.out.println("❌ Error fetching/saving content: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
