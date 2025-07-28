package com.joe_goldberge.utility;

import java.net.URL;

/**
 * Utility class for file name operations related to user uploads.
 * Provides methods to sanitize names, extract file names from URLs, and build new file names.
 */
public class FileNameUtil {

    /**
     * Cleans the first name: removes spaces, special chars, etc.
     * @param name The name to sanitize
     * @return Sanitized name with only alphanumeric characters and underscores
     */
    public static String sanitizeName(String name) {
        if (name == null) return "";
        // Remove non-alphanumeric, replace spaces with underscores
        return name.trim().replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * Extracts the filename (with extension) from the end of a URL
     * @param urlString The URL string
     * @return The file name extracted from the URL
     */
    public static String extractFileNameFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            String[] segments = path.split("/");
            return segments[segments.length - 1];
        } catch (Exception e) {
            // fallback: if URL is malformed, try simple split
            if (urlString == null) return "file_unknown";
            String[] segments = urlString.split("/");
            return segments[segments.length - 1];
        }
    }

    /**
     * Builds the new filename per your spec: userId_firstName_hg_oldFileName.extension
     * @param userId The user ID
     * @param firstName The user's first name
     * @param oldFileName The original file name
     * @return The new file name
     */
    public static String buildNewFileName(String userId, String firstName, String oldFileName) {
        String cleanFirstName = sanitizeName(firstName);
        if (oldFileName == null || oldFileName.isEmpty()) {
            oldFileName = "file_unknown";
        }
        return String.format("%s_%s_hg_%s", userId, cleanFirstName, oldFileName);
    }
}

