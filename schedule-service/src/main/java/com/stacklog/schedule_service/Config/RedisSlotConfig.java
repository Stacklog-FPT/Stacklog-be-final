package com.stacklog.schedule_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacklog.core_service.utils.jwt.JwtDecoder;
import com.stacklog.core_service.utils.redis.RedisService;
import com.stacklog.schedule_service.model.entities.Slot;
import com.stacklog.schedule_service.model.entities.SlotAssign;

@Configuration
public class RedisSlotConfig {
    
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("redis", 6379);
    }

    @Bean
    public RedisService<Slot> redisSlotService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            JwtDecoder jwtDecoder
    ) {
        return new RedisService<>(Slot.class, jwtDecoder, Slot::getSlotId);
    }

    @Bean
    public RedisService<SlotAssign> redisSlotAssignService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            JwtDecoder jwtDecoder
    ) {
        return new RedisService<>(SlotAssign.class, jwtDecoder, SlotAssign::getSlotAssignId);
    }

}
