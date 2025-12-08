package com.stacklog.class_service.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import com.stacklog.class_service.model.entities.Groupss;
import com.stacklog.class_service.model.repo.GroupsRepo;
import com.stacklog.core_service.model.service.IService;
import com.stacklog.core_service.utils.CommonFunction;
import com.stacklog.core_service.utils.kafka.KafkaProducer;
import com.stacklog.core_service.utils.redis.RedisService;

import jakarta.transaction.Transactional;

@Service
public class GroupService implements IService<Groupss> {

    private static final String NAME_SERVICE = "class-service";

    private static final String KAFKA_TOPIC_UPDATE = "class-service.groupsses.updated";
    private static final String KAFKA_TOPIC_CREATE = "class-service.groupsses.created";

    @Autowired
    GroupsRepo groupsRepo;

    @Autowired
    private KafkaProducer<Groupss> kafkaGroupsProducer;

    RedisService<Groupss> redisGroupsService;

    public GroupService(RedisService<Groupss> redisGroupsService) {
        this.redisGroupsService = redisGroupsService;
    }

    @Override
    @Transactional
    public Groupss delete(String id, String token) {
        Groupss groupss = redisGroupsService.getById(id, token, NAME_SERVICE);
        if (groupss == null) {
            groupss = groupsRepo.findById(id).orElseThrow();
        }
        groupsRepo.delete(groupss);
        redisGroupsService.deleteAllByUserId(token, NAME_SERVICE);
        return groupss;
    }

    @Override
    public List<Groupss> getAllByUserId(String token) {
        List<Groupss> groupsses = redisGroupsService.getAll(token, NAME_SERVICE);
        if (groupsses.isEmpty()) {
            groupsses = groupsRepo.findByUserId(redisGroupsService.getCurrentUserId(token));
            redisGroupsService.saveListToRedis(groupsses, token, NAME_SERVICE);
        }
        return groupsses;
    }

    @Override
    public Groupss getById(String id, String token) {
        Groupss groupss = redisGroupsService.getById(id, token, NAME_SERVICE);
        if (groupss == null) {
            groupss = groupsRepo.findById(id).orElseThrow();
            redisGroupsService.saveToRedis(groupss, token, NAME_SERVICE);
        }
        return groupss;
    }

    @Override
    public Groupss save(Groupss e, String token) {
        boolean isCreate = (e.getGroupsId() == null || !groupsRepo.existsById(e.getGroupsId()));
        e.setGroupsLeaderId(redisGroupsService.getCurrentUserId(token));
        Groupss newGroupss = saveToDB(e, token);
        if (newGroupss == null) {
            return null;
        }

        newGroupss = groupsRepo.findById(newGroupss.getGroupsId()).orElseThrow();

        if (isCreate) {
            kafkaGroupsProducer.sendMessage(newGroupss, KAFKA_TOPIC_CREATE);
        } else {
            kafkaGroupsProducer.sendMessage(newGroupss, KAFKA_TOPIC_UPDATE);
        }

        redisGroupsService.saveToRedis(newGroupss, token, NAME_SERVICE);

        return newGroupss;

    }

    @Transactional
    private Groupss saveToDB(Groupss e, String token) {
        e.setUpdateAt(CommonFunction.getCurrentTime());
        e.setUpdateBy(redisGroupsService.getCurrentUserId(token));
        if (e.getGroupsId() == null) {
            e.setCreatedAt(CommonFunction.getCurrentTime());
            e.setCreatedBy(redisGroupsService.getCurrentUserId(token));
            e.setGroupsId(UUID.randomUUID().toString());
        }
        return groupsRepo.save(e);
    }

    public List<Groupss> getAllByClassId(String token, String classesId) {
        // List<Groupss> groupsses = redisGroupsService.getAll(token, NAME_SERVICE);
        List<Groupss> groupsses = new ArrayList<>();
        if (groupsses.isEmpty()) {
            groupsses = groupsRepo.findByClassesClassesId(classesId);
            redisGroupsService.saveListToRedis(groupsses, token, NAME_SERVICE);
        }
        return groupsses;
    }

    public Groupss getGroupssByClassIdAndToken(String classId, String token) {
        List<Groupss> groupssList = groupsRepo.findByClassesClassesId(classId);
        for (Groupss groupss : groupssList) {
            if (groupss.getGroupStudents().stream()
                    .anyMatch(gs -> gs.getUserId().equals(redisGroupsService.getCurrentUserId(token)))) {
                return groupss;
            }
        }
        return null;
    }

    public List<Groupss> getGroupssBySemesterIdAndToken(String semesterId, String token) {
        return groupsRepo.findBySemesterIdAndUserId(semesterId, redisGroupsService.getCurrentUserId(token));
    }

    @KafkaListener(topics = "class-service.groupsses.find", groupId = "task-service.find-groups")
    @SendTo
    public List<String> onFindGroups(GroupssFindReq req) throws Exception {
        var ids = groupsRepo.findBySemesterIdAndUserId(req.semesterId(), req.token())
                .stream().map(Groupss::getGroupsId).toList();
        return ids;
    }

}

record GroupssFindReq(
        String semesterId,
        String token) {
}