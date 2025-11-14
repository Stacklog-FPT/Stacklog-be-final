package com.stacklog.task_service.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacklog.task_service.model.service.StatusTaskService;

@Service
public class AutoCreateStatustask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoCreateStatustask.class);

    @Autowired
    StatusTaskService statusTaskService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topicPattern = "class-service.groupsses.created", groupId = "class-service")
    public void consumerTask(String message) {
        try {
            GroupCreatedEvent evt = objectMapper.readValue(message, GroupCreatedEvent.class);
            String groupId = evt.groupsId();
            String userId = evt.createdBy();
            statusTaskService.createDefaultsForGroup(groupId, userId);
            LOGGER.info("Message received -> {}", message);
        } catch (Exception e) {
            LOGGER.error("❌ Error processing Kafka message: {}", message, e);
        }
    }

}

record GroupCreatedEvent(
        String groupsId,
        String groupName,
        String createdBy) {
}
