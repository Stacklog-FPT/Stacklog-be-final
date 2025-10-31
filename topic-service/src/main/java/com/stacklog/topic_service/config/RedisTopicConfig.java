package com.stacklog.topic_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacklog.core_service.utils.jwt.JwtDecoder;
import com.stacklog.core_service.utils.redis.RedisService;
import com.stacklog.topic_service.model.entities.ProjectInformation;

@Configuration
public class RedisTopicConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("redis", 6379);
    }

    @Bean
    public RedisService<ProjectInformation> redisProjectInformationService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            JwtDecoder jwtDecoder) {
        return new RedisService<>(ProjectInformation.class, jwtDecoder, ProjectInformation::getPiId);
    }

}
