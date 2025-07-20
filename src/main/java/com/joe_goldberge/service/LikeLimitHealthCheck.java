package com.joe_goldberge.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LikeLimitHealthCheck {

    private static final String ENDPOINT = "https://prod-api.hingeaws.net/likelimit";
    private static final String BEARER_TOKEN = System.getenv("BEARER_AUTH_TOKEN");
    private static final String SESSION_ID = System.getenv("SESSION_ID");


    public static void checkLikeLimit() throws Exception {
        URL url = new URL(ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Static headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "*/*");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "en-GB");
        headers.put("Authorization", BEARER_TOKEN);
        headers.put("Connection", "keep-alive");
        headers.put("Content-Type", "application/json");
        headers.put("Host", "prod-api.hingeaws.net");
        headers.put("User-Agent", "Hinge/11615 CFNetwork/3826.500.131 Darwin/24.5.0");
        headers.put("X-App-Version", "9.81.0");
        headers.put("X-Build-Number", "11615");
        headers.put("X-Device-Id", "491DB5EB-2C78-4774-9130-FB384A764D2F");
        headers.put("X-Device-Model", "unknown");
        headers.put("X-Device-Model-Code", "iPhone14,5");
        headers.put("X-Device-Platform", "iOS");
        headers.put("X-Device-Region", "IN");
        headers.put("X-Install-Id", "327E3786-75CB-49FB-A323-18DD0B343E44");
        headers.put("X-OS-Version", "18.5");
        headers.put("X-Session-Id", SESSION_ID);

        // Apply headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }

        int status = conn.getResponseCode();
        System.out.println("🔍 HTTP Status: " + status);

        if (status == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder body = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            reader.close();
            conn.disconnect();

            System.out.println("✅ Response Body:");
            System.out.println(body.toString());

            // You can parse and track specific params here if needed, like remaining likes or reset time
            // (optional feature, let me know if you want that)
        } else {
            throw new RuntimeException("Failed to reach endpoint. Status: " + status);
        }
    }
}
