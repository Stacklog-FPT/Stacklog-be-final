package com.stacklog.task_service.model.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stacklog.core_service.model.service.IService;
import com.stacklog.core_service.utils.CommonFunction;
import com.stacklog.core_service.utils.kafka.KafkaProducer;
import com.stacklog.core_service.utils.redis.RedisService;
import com.stacklog.task_service.model.entities.Review;
import com.stacklog.task_service.model.repo.ReviewRepo;

@Service
public class ReviewService implements IService<Review> {

    private static final String NAME_SERVICE = "task-service";

    private static final String KAFKA_TOPIC_UPDATE = "task-service.review.updated";
    private static final String KAFKA_TOPIC_CREATE = "task-service.review.created";

    @Autowired
    ReviewRepo reviewRepo;

    @Autowired
    KafkaProducer<Review> kafkaReviewProducer;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    RedisService<Review> redisReviewService;

    public ReviewService(RedisService<Review> redisReviewService) {
        this.redisReviewService = redisReviewService;
    }

    @Override
    public Review delete(String id, String token) {
        Review review = reviewRepo.findById(id).orElse(null);
        reviewRepo.delete(review);
        return review;
    }

    @Override
    public List<Review> getAllByUserId(String token) {
        // List<Review> reviews = redisReviewService.getAll(token, NAME_SERVICE);
        // if (reviews.isEmpty()) {
        // reviews =
        // reviewRepo.findByUserId(redisReviewService.getCurrentUserId(token));
        // redisReviewService.saveListToRedis(reviews, token, NAME_SERVICE);
        // }
        // return reviews;
        return null;
    }

    public List<Review> getAllByTaskId(String token, String taskId) {
        // List<Review> reviews = redisReviewService.getAll(taskId, NAME_SERVICE);
        // if (reviews.isEmpty()) {
        // reviews = reviewRepo.findByTaskId(taskId);
        // redisReviewService.saveListToRedis(reviews, taskId, NAME_SERVICE);
        // }
        List<Review> reviews = reviewRepo.findByTaskId(taskId);
        return reviews;
    }

    @Override
    public Review getById(String id, String token) {
        Review review = redisReviewService.getById(id, token, NAME_SERVICE);
        if (review == null) {
            review = reviewRepo.findById(id).orElseThrow();
            redisReviewService.saveToRedis(review, token, NAME_SERVICE);
        }
        return review;
    }

    @Override
    @Transactional
    public Review save(Review e, String token) {
        boolean isCreate = (e.getReviewId() == null || !reviewRepo.existsById(e.getReviewId()));
        e.setUpdateAt(CommonFunction.getCurrentTime());
        e.setUpdateBy(redisReviewService.getCurrentUserId(token));
        if (e.getReviewId() == null || e.getReviewId().isBlank()) {
            e.setCreatedAt(CommonFunction.getCurrentTime());
            e.setCreatedBy(redisReviewService.getCurrentUserId(token));
            e.setReviewId(UUID.randomUUID().toString());
        }
        if (isCreate) {
            kafkaReviewProducer.sendMessage(e, KAFKA_TOPIC_CREATE);
        } else {
            kafkaReviewProducer.sendMessage(e, KAFKA_TOPIC_UPDATE);
        }

        redisReviewService.saveToRedis(e, token, NAME_SERVICE);

        messagingTemplate.convertAndSend("/topic/task-service", e);

        e = reviewRepo.save(e);

        return e;

    }

    void deleteReviews(String taskId, List<Review> reviewsFE) {
        Set<String> keepIds = reviewsFE.stream()
                .map(Review::getReviewId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (keepIds == null || keepIds.isEmpty()) {
            reviewRepo.deleteAllByTaskId(taskId);
            return;
        }
        reviewRepo.deleteAllNotIn(taskId, keepIds);
    }

}
