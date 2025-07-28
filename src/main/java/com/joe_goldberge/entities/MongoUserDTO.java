package com.joe_goldberge.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MongoUserDTO {
    private String userId;
    private UserProfile userProfile;
    private Content content;
}
