package com.joe_goldberge.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserDetails {

    private static final String BEARER_AUTH_TOKEN = System.getenv("BEARER_AUTH_TOKEN");
    private static final String SESSION_ID = System.getenv("SESSION_ID");

    public static String getUserName(String userId) {
        try {
            String urlStr = "https://prod-api.hingeaws.net/user/v3/public?ids=" + userId;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Set headers
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

            int status = conn.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                conn.disconnect();

                System.out.println("👤 User Array: " + content.toString());

                JSONArray userArray = new JSONArray(content.toString());
                if (userArray.length() > 0) {
                    JSONObject userObj = userArray.getJSONObject(0);
                    JSONObject profile = userObj.optJSONObject("profile");
                    if (profile != null) {
                        return profile.optString("firstName", "Unknown");
                    }
                }
            } else {
                System.out.println("❌ Failed. HTTP status: " + status);
            }
        } catch (Exception e) {
            System.out.println("❌ Exception while fetching user name: " + e.getMessage());
            e.printStackTrace();
        }
        return "Unknown";
    }
}
