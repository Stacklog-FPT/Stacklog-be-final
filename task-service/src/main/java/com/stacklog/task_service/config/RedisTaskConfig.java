package com.stacklog.task_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacklog.core_service.utils.jwt.JwtDecoder;
import com.stacklog.core_service.utils.redis.RedisService;
import com.stacklog.task_service.model.entities.CheckItem;
import com.stacklog.task_service.model.entities.CheckList;
import com.stacklog.task_service.model.entities.Review;
import com.stacklog.task_service.model.entities.StatusTask;
import com.stacklog.task_service.model.entities.Task;
import com.stacklog.task_service.model.entities.TaskAssign;

@Configuration
public class RedisTaskConfig {

    // @Bean
    // public Function<Task, String> classIdExtractor() {
    // return Task::getTaskId;
    // }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("redis", 6379);
    }

    @Bean
    public RedisService<Task> redisTaskService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            JwtDecoder jwtDecoder) {
        return new RedisService<>(Task.class, jwtDecoder, Task::getTaskId);
    }

    @Bean
    public RedisService<Review> redisReviewService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            JwtDecoder jwtDecoder) {
        return new RedisService<>(Review.class, jwtDecoder, Review::getReviewId);
    }

    @Bean
    public RedisService<TaskAssign> redisTaskAssignService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            JwtDecoder jwtDecoder) {
        return new RedisService<>(TaskAssign.class, jwtDecoder, TaskAssign::getTaskAssignId);
    }

    @Bean
    public RedisService<CheckItem> redisCheckItemService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            JwtDecoder jwtDecoder) {
        return new RedisService<>(CheckItem.class, jwtDecoder, CheckItem::getCheckItemId);
    }

    @Bean
    public RedisService<CheckList> redisCheckListService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            JwtDecoder jwtDecoder) {
        return new RedisService<>(CheckList.class, jwtDecoder, CheckList::getCheckListId);
    }

    @Bean
    public RedisService<StatusTask> redisStatusTaskService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            JwtDecoder jwtDecoder) {
        return new RedisService<>(StatusTask.class, jwtDecoder, StatusTask::getStatusTaskId);
    }

}
