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
import com.stacklog.task_service.model.entities.TaskAssign;
import com.stacklog.task_service.model.repo.TaskAssignRepo;

@Service
public class TaskAssignService implements IService<TaskAssign> {

    private static final String NAME_SERVICE = "task-service";

    private static final String KAFKA_TOPIC_UPDATE = "task-service.taskassign.updated";
    private static final String KAFKA_TOPIC_CREATE = "task-service.taskassign.created";

    @Autowired
    TaskAssignRepo taskAssignRepo;

    @Autowired
    KafkaProducer<TaskAssign> taskAssignProducer;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    RedisService<TaskAssign> redisTaskAssignService;

    public TaskAssignService(RedisService<TaskAssign> redisTaskAssignService) {
        this.redisTaskAssignService = redisTaskAssignService;
    }

    @Override
    public TaskAssign delete(String id, String token) {
        return null;
    }

    @Override
    public List<TaskAssign> getAllByUserId(String token) {
        List<TaskAssign> taskAssigns = redisTaskAssignService.getAll(token, NAME_SERVICE);
        if (taskAssigns.isEmpty()) {
            taskAssigns = taskAssignRepo.findByAssignTo(redisTaskAssignService.getCurrentUserId(token));
            redisTaskAssignService.saveListToRedis(taskAssigns, token, NAME_SERVICE);
        }
        return taskAssigns;
    }

    @Override
    public TaskAssign getById(String id, String token) {
        TaskAssign taskAssign = redisTaskAssignService.getById(id, token, NAME_SERVICE);
        if (taskAssign == null) {
            taskAssign = taskAssignRepo.findById(id).orElseThrow();
            redisTaskAssignService.saveToRedis(taskAssign, token, NAME_SERVICE);
        }
        return taskAssign;
    }

    @Override
    @Transactional
    public TaskAssign save(TaskAssign e, String token) {
        TaskAssign taskAssign = taskAssignRepo.findByTaskTaskIdAndAssignTo(e.getTask().getTaskId(), e.getAssignTo());
        boolean isCreate = (taskAssign == null);
        e.setUpdateAt(CommonFunction.getCurrentTime());
        e.setUpdateBy(redisTaskAssignService.getCurrentUserId(token));
        if (isCreate) {
            e.setCreatedAt(CommonFunction.getCurrentTime());
            e.setCreatedBy(redisTaskAssignService.getCurrentUserId(token));
            e.setTaskAssignId(UUID.randomUUID().toString());
        } else {
            e.setTaskAssignId(taskAssign.getTaskAssignId());
        }
        if (isCreate) {
            taskAssignProducer.sendMessage(e, KAFKA_TOPIC_CREATE);
        } else {
            taskAssignProducer.sendMessage(e, KAFKA_TOPIC_UPDATE);
        }

        redisTaskAssignService.saveToRedis(e, token, NAME_SERVICE);

        messagingTemplate.convertAndSend("/topic/task-service", e);

        e = taskAssignRepo.save(e);

        return e;
    }

    public List<TaskAssign> getAllByTaskId(String token, String taskId) {
        List<TaskAssign> taskAssigns = taskAssignRepo.findByTaskTaskId(taskId);
        return taskAssigns;
    }

    public void deleteTaskAssigns(String taskId, List<TaskAssign> assignsFE) {
        Set<String> keepIds = assignsFE.stream()
                .map(TaskAssign::getTaskAssignId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (keepIds == null || keepIds.isEmpty()) {
            taskAssignRepo.deleteAllByTaskId(taskId);
            return;
        } 
        taskAssignRepo.deleteAllNotIn(taskId, keepIds);
    }

}
