package com.joe_goldberge.repository;

import com.joe_goldberge.entities.Users;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends MongoRepository<Users, String> {
    Users findByUserId(String userId);
}
