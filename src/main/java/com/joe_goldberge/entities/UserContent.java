package com.joe_goldberge.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("user_content")
public class UserContent {
    @Id
    private String id;
    private String userId;
    private Content content;

    @Data
    public static class Content {
        private List<Answer> answers;
        private List<Photo> photos;

        // Answer and Photo classes as inner or separate classes

        @Data // All getter/setter, equals, hashCode, toString
        public static class Answer {
            private String questionId;
            private String response;
            private String contentId;
            private Integer position;
            private TranscriptionMetadata transcriptionMetadata;

            @Data
            public static class TranscriptionMetadata {
                private String type;
            }
        }

        @Data
        public static class Photo {
            private String sourceId;
            private BoundingBox boundingBox;
            private String pHash;
            private String contentId;
            private Integer width;
            private String caption;
            private String cdnId;
            private String location;
            private String source;
            private Boolean selfieVerified;
            private String url;
            private Integer height;
            private String videoUrl;

            @Data
            public static class BoundingBox {
                private Point bottomRight;
                private Point topLeft;

                @Data
                public static class Point {
                    private Integer x;
                    private Integer y;
                }
            }
        }
    }
}