package com.joe_goldberge;

import com.joe_goldberge.service.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@EnableScheduling
@SpringBootApplication
public class JoeGoldbergeApplication {

    public static void main(String[] args) {
//        uploadFileByLink("https://media.hingenexus.com/video/upload/f_mp4,w_720,c_limit/ymntcyawqjrjc555e02i.mp4","cute_cat.mp4");
        SpringApplication.run(JoeGoldbergeApplication.class, args);
    }

    /// /// //////////////////////////////////////////----------------/////////////////////////////////////
    ///
    ///

    // write a methond to call uploadByLink function from TeraBoxService

    public static void uploadFileByLink(String link, String fileName) {

        try {
//            TeraBoxService.uploadFileByLink(link, fileName);
            System.out.println("✅ File uploaded successfully.");
        } catch (Exception e) {
            System.out.println("❌ Error uploading file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void fetchAndSaveUsersFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter user IDs separated by commas:");
        String input = scanner.nextLine();

        List<String> userIds = Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

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
    }


    public static void getUserName() {
        String userId = "3618689760702235660";
        UserDetails userDetails = new UserDetails();
        String userName = userDetails.getUserName(userId);

        System.out.println("👤 User Name: " + userName);
    }

    public static void getAndUploadUserDetails() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter user IDs separated by commas:");
        String input = scanner.nextLine();
        scanner.close();

        // Remove whitespace, split and create list
        List<String> userIds = Arrays.asList(input.replaceAll("\\s+", "").split(","));
        ContentService contentService = new ContentService();
        contentService.processUserContents(userIds);
    }

    public static void doHealthCheck() {
        LikeLimitHealthCheck healthCheck = new LikeLimitHealthCheck();
        try {
            healthCheck.checkLikeLimit();
        } catch (Exception e) {
            System.out.println("❌ Health check failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("✅ Health check completed successfully.");
    }

}
