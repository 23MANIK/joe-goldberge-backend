package com.joe_goldberge.entities;

import lombok.Data;
import java.util.List;

@Data
public class Content {
    private List<Answer> answers;
    private List<Photo> photos;

    @Data
    public static class Answer {
        private String questionId;
        private String response;
        private String contentId;
        private int position;
        private Object transcriptionMetadata; // Replace with specific type if known
        private String type;
    }

    @Data
    public static class Photo {
        private String sourceId;
        private BoundingBox boundingBox;
        private String pHash;
        private String contentId;
        private int width;
        private String caption;
        private String cdnId;
        private String location;
        private String source;
        private boolean selfieVerified;
        private String url;
        private int height;
        private String videoUrl;
    }

    @Data
    public static class BoundingBox {
        private Point bottomRight;
        private Point topLeft;
    }

    @Data
    public static class Point {
        private double x;
        private double y;
    }
}
