package com.joe_goldberge.repository;

import com.joe_goldberge.entities.UserContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserContentRepository extends MongoRepository<UserContent, String> {
    UserContent findByUserId(String userId);
}