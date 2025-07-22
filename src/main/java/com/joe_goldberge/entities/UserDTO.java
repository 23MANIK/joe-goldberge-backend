package com.joe_goldberge.entities;

import lombok.Data;
import java.util.List;

@Data
public class UserDTO {
    private String userId;
    private String firstName;
    private String lastName;
    private Integer age;
    private String hometown;
    private String jobTitle;
    private List<String> education;
    private String location;
    private List<UserContent.Content.Answer> answers;
    private List<UserContent.Content.Photo> photos;


    // Constructor for assembling DTO from entity classes
    public UserDTO(Users user, UserContent content) {
        this.userId = user.getUserId();
        this.firstName = user.getProfile().getFirstName();
        this.lastName = user.getProfile().getLastName();
        this.age = user.getProfile().getAge();
        this.hometown = user.getProfile().getHometown();
        this.jobTitle = user.getProfile().getJobTitle();
        this.education = user.getProfile().getEducation();
        this.location = user.getProfile().getLocation();
        this.answers = content != null ? content.getContent().getAnswers() : null;
        this.photos = content != null ? content.getContent().getPhotos() : null;

    }
}