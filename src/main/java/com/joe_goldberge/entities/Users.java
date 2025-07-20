package com.joe_goldberge.entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Document("users")
public class Users {
    @Id
    private String id;
    private String userId;
    private Profile profile;

    @Data
    public static class Profile {

        private String id;
        private String userId;
        private String firstName;

        private String lastName;
        private Integer age;
        private String hometown;
        private String jobTitle;
        private List<String> education;
        private String location;
        // getters and setters
    }
}