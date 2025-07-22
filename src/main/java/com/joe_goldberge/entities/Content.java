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

    }

    @Data
    public static class Photo {
//        private BoundingBox boundingBox;
        private boolean selfieVerified;
        private String url;
//        private int height;
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
