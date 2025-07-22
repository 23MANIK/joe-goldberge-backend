package com.joe_goldberge.entities;

import lombok.Data;
import java.util.List;

@Data
public class UserProfile {
    private String lastName;
    private String hometown;
    private List<String> education;
    private String firstName;
    private int lastActiveStatusId;
    private Location location;
    private boolean didjustJoin;
    private int age;
    private int height;

    @Data
    public static class Location {
        private String name;
    }
}
