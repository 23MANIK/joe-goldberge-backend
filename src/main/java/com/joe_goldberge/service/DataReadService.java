package com.joe_goldberge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import com.joe_goldberge.entities.MongoUserDTO;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class DataReadService {

    private static final Logger logger = LoggerFactory.getLogger(DataReadService.class);
    private final MongoTemplate mongoTemplate;

    @Autowired
    public DataReadService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Page<MongoUserDTO> getAllUsers(
            int page,
            int size,
            String firstName,
            String education,
            String location,
            String hometown
    ) {
        logger.info("getAllUsers called with page: {}, size: {}, firstName: {}, education: {}, location: {}, hometown: {}",
                page, size, firstName, education, location, hometown);

        Pageable pageable = PageRequest.of(page, size);
        List<Criteria> criteriaList = new ArrayList<>();

        if (firstName != null && !firstName.isEmpty()) {
            criteriaList.add(Criteria.where("userProfile.profile.firstName").regex(Pattern.compile(Pattern.quote(firstName), Pattern.CASE_INSENSITIVE)));
        }
        if (education != null && !education.isEmpty()) {
            criteriaList.add(Criteria.where("userProfile.profile.education").regex(Pattern.compile(Pattern.quote(education), Pattern.CASE_INSENSITIVE)));
        }
        if (location != null && !location.isEmpty()) {
            criteriaList.add(Criteria.where("userProfile.profile.location").regex(Pattern.compile(Pattern.quote(location), Pattern.CASE_INSENSITIVE)));
        }
        if (hometown != null && !hometown.isEmpty()) {
            criteriaList.add(Criteria.where("userProfile.profile.hometown").regex(Pattern.compile(Pattern.quote(hometown), Pattern.CASE_INSENSITIVE)));
        }

        Criteria criteria = criteriaList.isEmpty() ? new Criteria() : new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        logger.info("Filtering criteria built: {}", criteria);

        // Aggregation stages:
        MatchOperation matchStage = Aggregation.match(criteria);
        LookupOperation lookupStage = Aggregation.lookup("users", "userId", "userId", "userProfile");
        UnwindOperation unwindStage = Aggregation.unwind("userProfile", true);
        SkipOperation skipStage = Aggregation.skip((long) pageable.getOffset());
        LimitOperation limitStage = Aggregation.limit(pageable.getPageSize());

        ProjectionOperation projectStage = Aggregation.project()
                .and("userId").as("userId")
                .and("content").as("content")
                .and("userProfile.profile").as("userProfile");

        // Count total matching docs
        Aggregation countAgg = Aggregation.newAggregation(
                matchStage,
                lookupStage,
                unwindStage,
                Aggregation.count().as("total")
        );
        long total = 0L;
        List<org.bson.Document> countResult = mongoTemplate.aggregate(countAgg, "user_content", org.bson.Document.class).getMappedResults();
        if (!countResult.isEmpty()) {
            Object t = countResult.get(0).get("total");
            total = t instanceof Number ? ((Number)t).longValue() : 0L;
        }
        logger.info("Total documents matching criteria: {}", total);

        Aggregation aggregation = Aggregation.newAggregation(
                lookupStage,
                unwindStage,
                matchStage,
                skipStage,
                limitStage,
                projectStage
        );
        List<MongoUserDTO> content = mongoTemplate.aggregate(aggregation, "user_content", MongoUserDTO.class).getMappedResults();
        logger.info("Returning {} documents", content.size());

        return new PageImpl<>(content, pageable, total);
    }
}