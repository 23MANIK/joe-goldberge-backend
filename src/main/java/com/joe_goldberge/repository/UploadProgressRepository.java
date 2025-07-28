package com.joe_goldberge.repository;

import com.joe_goldberge.entities.UploadProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UploadProgressRepository extends MongoRepository<UploadProgress, String> {
    Optional<UploadProgress> findTopByOrderByStartedAtDesc();
}
