package com.joe_goldberge.entities;

import lombok.Data;
import java.util.List;

@Data
public class UserProfile {
    private int datingIntention;
    private String datingIntentionText;
    private String lastName;
    private String hometown;
    private int drinking;
    private int drugs;
    private List<Integer> sexualOrientations;
    private List<String> education;
    private int marijuana;
    private List<Integer> ethnicities;
    private String firstName;
    private int genderIdentityId;
    private int children;
    private int smoking;
    private List<Integer> religions;
    private int lastActiveStatusId;
    private Location location;
    private List<Integer> pronouns;
    private List<Integer> languagesSpoken;
    private boolean didjustJoin;
    private int age;
    private int height;

    @Data
    public static class Location {
        private String name;
    }
}
