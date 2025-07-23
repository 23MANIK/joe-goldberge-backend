package com.joe_goldberge.service;

import com.joe_goldberge.entities.UserContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
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
            criteriaList.add(Criteria.where("profile.firstName")
                .regex(Pattern.compile("^" + Pattern.quote(firstName), Pattern.CASE_INSENSITIVE)));
        }
        if (education != null && !education.isEmpty()) {
            criteriaList.add(Criteria.where("profile.education")
                .regex(Pattern.compile("^" + Pattern.quote(education), Pattern.CASE_INSENSITIVE)));
        }
        if (location != null && !location.isEmpty()) {
            criteriaList.add(Criteria.where("profile.location.name")
                .regex(Pattern.compile("^" + Pattern.quote(location), Pattern.CASE_INSENSITIVE)));
        }
        if (hometown != null && !hometown.isEmpty()) {
            criteriaList.add(Criteria.where("profile.hometown")
                .regex(Pattern.compile("^" + Pattern.quote(hometown), Pattern.CASE_INSENSITIVE)));
        }

        Criteria criteria = criteriaList.isEmpty() ? new Criteria() : new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        logger.info("Filtering criteria built: {}", criteria);

        // Aggregation stages:
        MatchOperation matchStage = Aggregation.match(criteria);
        SortOperation sortStage = Aggregation.sort(Sort.by(Sort.Direction.ASC, "profile.firstName"));
        SkipOperation skipStage = Aggregation.skip((long) pageable.getOffset());
        LimitOperation limitStage = Aggregation.limit(pageable.getPageSize());

        ProjectionOperation projectStage = Aggregation.project()
                .and("userId").as("userId")
                .and("content").as("content")
                .and("profile").as("userProfile");

        // Count total matching docs
        Aggregation countAgg = Aggregation.newAggregation(
                matchStage,
                Aggregation.count().as("total")
        );
        long total = 0L;
        List<org.bson.Document> countResult = mongoTemplate.aggregate(countAgg, "users", org.bson.Document.class).getMappedResults();
        if (!countResult.isEmpty()) {
            Object t = countResult.getFirst().get("total");
            total = t instanceof Number ? ((Number)t).longValue() : 0L;
        }
        logger.info("Total documents matching criteria: {}", total);

        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                sortStage,
                skipStage,
                limitStage,
                projectStage
        );
        List<MongoUserDTO> content = mongoTemplate.aggregate(aggregation, "users", MongoUserDTO.class).getMappedResults();
        logger.info("Returning {} documents", content.size());

        return new PageImpl<>(content, pageable, total);
    }


    public List<UserContent> getAllContents( List<String> userIds) {
        // get ddata from user_content collection
        logger.info("getAllContents called with userIds: {}", userIds);
        if (userIds == null || userIds.isEmpty()) {
            logger.warn("No user IDs provided for content retrieval.");
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("userId").in(userIds);
        MatchOperation matchStage = Aggregation.match(criteria);
        ProjectionOperation projectStage = Aggregation.project()
                .and("userId").as("userId")
                .and("content").as("content");
//                .and("createdAt").as("createdAt");

        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                projectStage
        );
        List<UserContent> content = mongoTemplate.aggregate(aggregation, "user_content", UserContent.class).getMappedResults();
        logger.info("Returning {} user content documents", content.size());
        return content;
    }
}