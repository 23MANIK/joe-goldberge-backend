package com.joe_goldberge.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONObject;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class ContentService {
    private Cloudinary cloudinary;
    private static final String BEARER_TOKEN = System.getenv("BEARER_AUTH_TOKEN");
    private static final String SESSION_ID = System.getenv("SESSION_ID");
    private String name;


    public ContentService() {
        // Initialize Cloudinary with environment variables
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dhwoxrzcp",
                "api_key", "572986715448225",
                "api_secret", System.getenv("CLOUDINARY_API_SECRET")
        ));
    }

    public List<String> getContentUrls(String userId) throws Exception {
        List<String> urls = new ArrayList<>();
        String urlStr = "https://prod-api.hingeaws.net/content/v2/public?ids=" + userId;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");



        // Set headers
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "en-GB");
        conn.setRequestProperty("Authorization", BEARER_TOKEN);
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

        int status = conn.getResponseCode();
        if (status == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            // Parse JSON & extract URLs
            JSONArray dataArray = new JSONArray(content.toString());
            if (dataArray.length() > 0) {
                JSONObject contentObj = dataArray.getJSONObject(0).getJSONObject("content");
                JSONArray photos = contentObj.getJSONArray("photos");

                for (int i = 0; i < photos.length(); i++) {
                    JSONObject media = photos.getJSONObject(i);
                    String imageUrl = media.optString("url", "");
                    String videoUrl = media.optString("videoUrl", "");


                    if (!imageUrl.isEmpty()) {
                        urls.add(imageUrl);
                    }
                    if (!videoUrl.isEmpty()) {
                        urls.add(videoUrl);
                    }
                }
            }
        } else {
            throw new Exception("Failed to fetch content. HTTP Status: " + status);
        }
        this.name = UserDetails.getUserName(userId);
        return urls;
    }

    public String uploadToCloudinary(String mediaUrl,String userId) throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("use_filename", true);
        options.put("unique_filename", false);
        options.put("overwrite", true);

        options.put("folder", this.name + "(" + userId + ")"); // 🗂️ Store in a folder named after the user

        // 👀 Detect if it's a video
        if (mediaUrl.endsWith(".mp4") || mediaUrl.contains("/video/upload")) {
            options.put("resource_type", "video");
        }

        Map uploadResult = cloudinary.uploader().upload(mediaUrl, options);
        return (String) uploadResult.get("secure_url");
    }

    ////// main funciton of the class
    public void processUserContent(String userId) {
        try {
            System.out.println("🔍 Fetching content for user: " + userId);
            List<String> urls = getContentUrls(userId);

            System.out.println("📋 Found " + urls.size() + " media files:");
            for (String url : urls) {
                System.out.println("📎 " + url);
            }

            System.out.println("\n☁️ Uploading to Cloudinary...");
            for (String url : urls) {
                try {
                    if (url.contains("/video/upload/so_0p/") && url.endsWith(".jpeg")) {
                        System.out.println("⏭️ Skipping video preview: " + url);
                        continue;
                    }
                    String cloudinaryUrl = uploadToCloudinary(url, userId);
                    System.out.println("✅ Uploaded: " + cloudinaryUrl);
                } catch (Exception e) {
                    System.out.println("❌ Failed to upload: " + url + " - " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error processing content: " + e.getMessage());
            e.printStackTrace();
        }
    }

// ==================================================================

    // 1. Builds the request URL
    private String buildRequestUrl(List<String> userIds) {
        String idsParam = String.join(",", userIds);
        return "https://prod-api.hingeaws.net/content/v2/public?ids=" + idsParam;
    }

    // 2. Applies headers for connection
    private void setHeaders(HttpURLConnection conn) {
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "en-GB");
        conn.setRequestProperty("Authorization", BEARER_TOKEN);
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
    }

    // 3. Issues the HTTP call and returns the raw response
    private String fetchContentResponse(String requestUrl) throws Exception {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        setHeaders(conn);

        int status = conn.getResponseCode();
        if (status == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();
            return content.toString();
        } else {
            throw new Exception("Failed to fetch content. HTTP Status: " + status);
        }
    }

    // 4. Parses the response JSON and builds the mapping userId -> URLs
    private Map<String, List<String>> parseContentUrls(String jsonResponse) {
        Map<String, List<String>> userUrlMap = new HashMap<>();
        JSONArray dataArray = new JSONArray(jsonResponse);
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject userObj = dataArray.getJSONObject(i);
            String userId = null;
            // Try to get userId as string, if not, as number
            if(userObj.has("userId")) {
                Object idObj = userObj.get("userId");
                userId = idObj.toString(); // Handles both numeric and string
            } else if(userObj.has("id")) {
                // fallback if "id" is used instead of "userId"
                Object idObj = userObj.get("id");
                userId = idObj.toString();
            } else {
                System.out.println("No userId or id found for object: " + userObj.toString());
                continue; // Skip this entry
            }

            List<String> urls = new ArrayList<>();
            if (userObj.has("content")) {
                JSONObject contentObj = userObj.getJSONObject("content");
                JSONArray photos = contentObj.optJSONArray("photos");
                if (photos != null) {
                    for (int j = 0; j < photos.length(); j++) {
                        JSONObject media = photos.getJSONObject(j);
                        String imageUrl = media.optString("url", "");
                        String videoUrl = media.optString("videoUrl", "");
                        if (!imageUrl.isEmpty()) urls.add(imageUrl);
                        if (!videoUrl.isEmpty()) urls.add(videoUrl);
                    }
                }
            }
            userUrlMap.put(userId, urls);
        }
        return userUrlMap;
    }


    // Public method: the orchestration method, following SRP
    public Map<String, List<String>> getContentUrls(List<String> userIds) throws Exception {
        String requestUrl = buildRequestUrl(userIds);               // Step 1
        String jsonResponse = fetchContentResponse(requestUrl);     // Step 3
        return parseContentUrls(jsonResponse);                      // Step 4
    }


    public void processUserContents(List<String> userIds) {
        try {
            // Fetch all users' media URLs in a single call
            Map<String, List<String>> allUrls = getContentUrls(userIds);

            for (String userId : userIds) {
                System.out.println("\n🔍 Fetching content for user: " + userId);
                List<String> urls = allUrls.getOrDefault(userId, Collections.emptyList());

                System.out.println("📋 Found " + urls.size() + " media files:");
                for (String url : urls) {
                    System.out.println("📎 " + url);
                }

                System.out.println("☁️ Uploading to Cloudinary...");
                uploadAllMediaParallely(urls,userId);
//                for (String url : urls) {
//                    try {
//                        if (url.contains("/video/upload/so_0p/") && url.endsWith(".jpeg")) {
//                            System.out.println("⏭️ Skipping video preview: " + url);
//                            continue;
//                        }
//                        String cloudinaryUrl = uploadToCloudinary(url, userId);
//                        System.out.println("✅ Uploaded: " + cloudinaryUrl);
//                    } catch (Exception e) {
//                        System.out.println("❌ Failed to upload: " + url + " - " + e.getMessage());
//                    }
//                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error processing content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void uploadAllMediaParallely(List<String> urls, String userId) throws InterruptedException {
        long start = System.currentTimeMillis();

        int poolSize = 10; // Start with 10 as per Cloudinary recommendation[7]
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        List<Future<?>> futures = new ArrayList<>();

        for (String url : urls) {
            futures.add(executor.submit(() ->{
                try {
                    if (url.contains("/video/upload/so_0p/") && url.endsWith(".jpeg")) {
                        System.out.println("⏭️ Skipping video preview: " + url);
                    }else {
                        String cloudinaryUrl = uploadToCloudinary(url, userId);
                        System.out.println("✅ Uploaded: " + cloudinaryUrl);
                    }
                } catch (Exception e) {
                    System.out.println("❌ Failed to upload: " + url + " - " + e.getMessage());
                }
            }));
        }

        // Wait for all uploads to complete
        for (Future<?> f : futures) {
            try {
                f.get(); // Will throw if upload failed (you can handle per-task exception)
            } catch (Exception e) {
                // Already logged above
            }
        }

        executor.shutdown();

        long end = System.currentTimeMillis();
        System.out.println("⏱️ Total upload time: " + (end - start) + " ms");
    }

}
