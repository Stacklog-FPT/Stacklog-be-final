package com.stacklog.topic_service.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.stacklog.core_service.model.service.IService;
import com.stacklog.core_service.utils.CommonFunction;
import com.stacklog.core_service.utils.kafka.KafkaProducer;
import com.stacklog.core_service.utils.redis.RedisService;
import com.stacklog.topic_service.model.entities.ProjectInformation;
import com.stacklog.topic_service.model.repo.ProjectInformationRepo;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class ProjectInformationService implements IService<ProjectInformation> {

    private static final String NAME_SERVICE = "topic-service";
    private static final String GROUP_SUFFIX_PREFIX = "group:";

    private static final String KAFKA_TOPIC_UPDATE = "topic-service.projectinformation.updated";
    private static final String KAFKA_TOPIC_CREATE = "topic-service.projectinformation.created";

    @Autowired
    private ProjectInformationRepo piRepo;
    @Autowired
    private KafkaProducer<ProjectInformation> kafkaProjectInformationProducer;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ClassServiceClient classServiceClient;

    @Autowired
    EntityManager entityManager;

    private final RedisService<ProjectInformation> redisPIService;

    public ProjectInformationService(RedisService<ProjectInformation> redisPIService) {
        this.redisPIService = redisPIService;
    }

    @Override
    public ProjectInformation delete(String id, String token) {
        ProjectInformation projectInformation = piRepo.findById(id).orElseThrow();
        piRepo.deleteById(id);

        redisPIService.deleteAllByUserId(token, NAME_SERVICE);

        return projectInformation;

    }

    @Override
    public List<ProjectInformation> getAllByUserId(String token) {
        // List<ProjectInformation> lists = redisPIService.getAll(token, NAME_SERVICE);
        // if (lists == null || lists.isEmpty()) {
        //     lists = piRepo.findAllByUserId(redisPIService.getCurrentUserId(token));
        //     redisPIService.saveListToRedis(lists, token, NAME_SERVICE);
        // }
        // return lists;
        return null;
    }

    public ProjectInformation getByGroupId(String groupId, String token) {
        ProjectInformation pi = redisPIService.getById(groupId, token, NAME_SERVICE);
        if (pi == null) {
            pi = piRepo.findByGroupId(groupId).orElseThrow();
            redisPIService.saveToRedis(pi, token, NAME_SERVICE);
        }
        return pi;
    }

    public List<ProjectInformation> getAllByClassId(String classId, String token) {
        List<String> groupIds = classServiceClient.getGroupssByClassId(token, classId)
                .stream()
                .map(Groupss::getGroupsId)
                .filter(id -> id != null && !id.isBlank())
                .toList();

        if (groupIds.isEmpty())
            return List.of();

        // 2) lấy userId hiện tại từ Redis (như bạn đang làm)
        String userId = redisPIService.getCurrentUserId(token);

        // 3) query DB 1 lần cho tất cả group
        List<ProjectInformation> piList = piRepo.findAllByGroupIds(groupIds);

        // 4) warm cache theo group để lần sau nhanh hơn
        for (String gid : groupIds) {
            String suffix = "group:" + gid;
            redisPIService.saveListToRedisWithSuffix(
                    piList.stream().filter(t -> gid.equals(t.getGroupId())).toList(),
                    token, NAME_SERVICE, suffix);
        }
        // 5) cũng có thể fill cache tổng theo user nếu cần
        redisPIService.saveListToRedis(piList, token, NAME_SERVICE);

        return piList;

    }

    @Override
    public ProjectInformation getById(String id, String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }

    @Override
    @Transactional
    public ProjectInformation save(ProjectInformation e, String token) {
        boolean isCreate = (e.getPiId() == null || !piRepo.existsById(e.getPiId()));

        e = saveToDB(e, token, isCreate);

        entityManager.flush();
        entityManager.clear();

        ProjectInformation pi = new ProjectInformation();

        pi = piRepo.findById(e.getPiId()).orElse(null);

        // Cập nhật cache index tổng theo user
        redisPIService.saveToRedis(pi, token, NAME_SERVICE);

        // Nếu có group → cập nhật index theo group
        if (e.getGroupId() != null) {
            String suffix = GROUP_SUFFIX_PREFIX + pi.getGroupId();
            redisPIService.saveToRedisWithSuffix(pi, token, NAME_SERVICE, suffix);
        }

        kafkaProjectInformationProducer.sendMessage(pi, isCreate ? KAFKA_TOPIC_CREATE : KAFKA_TOPIC_UPDATE);
        messagingTemplate.convertAndSend("/topic/topic-service", pi);
        return pi;
    }

    @Transactional
    private ProjectInformation saveToDB(ProjectInformation e, String token, boolean isCreate) {
        LocalDateTime now = CommonFunction.getCurrentTime();
        String currentUserId = redisPIService.getCurrentUserId(token);

        e.setUpdateAt(now);
        e.setUpdateBy(currentUserId);
        if (isCreate) {
            e.setCreatedAt(now);
            e.setCreatedBy(currentUserId);
            e.setPiId(UUID.randomUUID().toString());
        }

        e.setPiId(piRepo.save(e).getPiId());
        return e;
    }

}
