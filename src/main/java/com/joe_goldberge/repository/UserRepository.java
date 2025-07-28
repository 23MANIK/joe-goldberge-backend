package com.joe_goldberge.repository;

import com.joe_goldberge.entities.MongoUserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<MongoUserDTO, String> {
    Page<MongoUserDTO> findAll(Pageable pageable);
    // Add custom query methods if needed
}

