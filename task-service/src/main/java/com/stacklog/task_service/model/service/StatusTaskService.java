package com.stacklog.task_service.model.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stacklog.core_service.model.service.IService;
import com.stacklog.core_service.utils.CommonFunction;
import com.stacklog.core_service.utils.kafka.KafkaProducer;
import com.stacklog.core_service.utils.redis.RedisService;
import com.stacklog.task_service.model.entities.StatusTask;
import com.stacklog.task_service.model.repo.StatusTaskRepo;

@Service
public class StatusTaskService implements IService<StatusTask> {

    private static final String NAME_SERVICE = "task-service";

    private static final String GROUP_SUFFIX_PREFIX = "group:";

    private static final String KAFKA_TOPIC_UPDATE = "task-service.statustask.updated";
    private static final String KAFKA_TOPIC_CREATE = "task-service.statustask.created";

    @Autowired
    StatusTaskRepo statusTaskRepo;

    @Autowired
    KafkaProducer<StatusTask> statusTaskProducer;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    RedisService<StatusTask> redisStatusTaskService;

    public StatusTaskService(RedisService<StatusTask> redisStatusTaskService) {
        this.redisStatusTaskService = redisStatusTaskService;
    }

    @Override
    public StatusTask delete(String id, String token) {
        StatusTask statusTask = statusTaskRepo.findById(id).orElseThrow();
        String userId = redisStatusTaskService.getCurrentUserId(token);

        statusTaskRepo.deleteById(id);

        List<StatusTask> byUser = statusTaskRepo.findByCreatedBy(userId);
        redisStatusTaskService.saveListToRedis(byUser, token, NAME_SERVICE);

        if (statusTask.getGroupId() != null) {
            String suffix = GROUP_SUFFIX_PREFIX + statusTask.getGroupId();
            List<StatusTask> byGroup = statusTaskRepo.findAllByGroupId(statusTask.getGroupId());
            redisStatusTaskService.saveListToRedisWithSuffix(byGroup, token, NAME_SERVICE, suffix);
        }

        return statusTask;

    }

    @Override
    public List<StatusTask> getAllByUserId(String token) {
        return null;
    }

    public List<StatusTask> getAllByGroupId(String token, String groupId) {
        List<StatusTask> statusTasks = redisStatusTaskService.getAll(token, NAME_SERVICE).stream()
                .filter(st -> st.getGroupId().equals(groupId)).toList();
        if (statusTasks.isEmpty() || statusTasks == null) {
            statusTasks = statusTaskRepo.findAllByGroupId(groupId);
            redisStatusTaskService.saveListToRedis(statusTasks, token, NAME_SERVICE);
        }
        return statusTasks;
    }

    @Override
    public StatusTask getById(String id, String token) {
        StatusTask statusTask = redisStatusTaskService.getById(id, token, NAME_SERVICE);
        if (statusTask == null) {
            statusTask = statusTaskRepo.findById(id).orElseThrow();
            redisStatusTaskService.saveToRedis(statusTask, token, NAME_SERVICE);
        }
        return statusTask;
    }

    @Override
    @Transactional
    public StatusTask save(StatusTask e, String token) {
        boolean isCreate = (e.getStatusTaskId() == null || !statusTaskRepo.existsById(e.getStatusTaskId()));
        e.setUpdateAt(CommonFunction.getCurrentTime());
        e.setUpdateBy(redisStatusTaskService.getCurrentUserId(token));
        if (e.getStatusTaskId() == null) {
            e.setCreatedAt(CommonFunction.getCurrentTime());
            e.setCreatedBy(redisStatusTaskService.getCurrentUserId(token));
            e.setStatusTaskId(UUID.randomUUID().toString());
        }

        if (e.getGroupId() != null) {
            String suffix = GROUP_SUFFIX_PREFIX + e.getGroupId();
            redisStatusTaskService.saveToRedisWithSuffix(e, token, NAME_SERVICE, suffix);
        }

        redisStatusTaskService.saveToRedis(e, token, NAME_SERVICE);

        statusTaskProducer.sendMessage(e, isCreate ? KAFKA_TOPIC_CREATE : KAFKA_TOPIC_UPDATE);

        messagingTemplate.convertAndSend("/topic/task-service", e);

        e = statusTaskRepo.save(e);

        return e;
    }

    public List<StatusTask> createDefaultsForGroup(String groupId, String userId) {
        for (int i = 0; i < 4; i++) {
            StatusTask defaulStatusTask = new StatusTask();
            defaulStatusTask.setStatusTaskId(UUID.randomUUID().toString());
            defaulStatusTask.setCreatedAt(CommonFunction.getCurrentTime());
            defaulStatusTask.setCreatedBy(userId);
            defaulStatusTask.setUpdateAt(CommonFunction.getCurrentTime());
            defaulStatusTask.setUpdateBy(userId);
            defaulStatusTask.setGroupId(groupId);
            switch (i) {
                case 0:
                    defaulStatusTask.setStatusTaskName("TODO");
                    defaulStatusTask.setStatusTaskColor("blue");
                    break;
                case 1:
                    defaulStatusTask.setStatusTaskName("DOING");
                    defaulStatusTask.setStatusTaskColor("blue");
                    break;
                case 2:
                    defaulStatusTask.setStatusTaskName("DONE");
                    defaulStatusTask.setStatusTaskColor("blue");
                    break;
                case 3:
                    defaulStatusTask.setStatusTaskName("COMPLETED");
                    defaulStatusTask.setStatusTaskColor("blue");
                    break;
                default:
                    break;
            }
            statusTaskRepo.save(defaulStatusTask);

        }
        return statusTaskRepo.findAllByGroupId(groupId);

    }

}
