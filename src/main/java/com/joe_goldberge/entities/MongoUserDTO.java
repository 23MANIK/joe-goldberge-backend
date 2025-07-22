package com.joe_goldberge.entities;

import lombok.Data;

@Data
public class MongoUserDTO {
    private String userId;
    private UserProfile userProfile;
    private Content content;
}
