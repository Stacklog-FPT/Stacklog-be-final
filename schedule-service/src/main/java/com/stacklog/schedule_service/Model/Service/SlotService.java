package com.stacklog.schedule_service.model.service;

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
import com.stacklog.schedule_service.model.entities.Slot;
import com.stacklog.schedule_service.model.repo.SlotRepo;

import jakarta.persistence.EntityManager;

@Service
public class SlotService implements IService<Slot> {

    private static final String NAME_SERVICE = "schedule-service";

    private static final String KAFKA_TOPIC_UPDATE = "schedule-service.slot.updated";
    private static final String KAFKA_TOPIC_CREATE = "schedule-service.slot.created";

    @Autowired
    private SlotRepo slotRepo;

    @Autowired
    private SlotAssignService slotAssignService;

    @Autowired
    ClassServiceClient classServiceClient;

    @Autowired
    private KafkaProducer<Slot> kafkaSlotProducer;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private EntityManager entityManager;

    RedisService<Slot> redisSlotService;

    public SlotService(RedisService<Slot> redisSlotService) {
        this.redisSlotService = redisSlotService;
    }

    @Override
    public Slot delete(String id, String token) {
        Slot slot = getById(id, token);
        slotRepo.delete(slot);
        List<Slot> slots = redisSlotService.getAll(token, NAME_SERVICE);
        slots.remove(slot);
        redisSlotService.saveListToRedis(slots, token, NAME_SERVICE);
        return slot;
    }

    @Override
    public List<Slot> getAllByUserId(String token) {
        List<Slot> slotes = redisSlotService.getAll(token, NAME_SERVICE);
        if (slotes.isEmpty()) {
            slotes = slotRepo.findByUserId(redisSlotService.getCurrentUserId(token));
            redisSlotService.saveListToRedis(slotes, token, NAME_SERVICE);
        }
        return slotes;
    }

    @Override
    public Slot getById(String id, String token) {
        Slot slot = redisSlotService.getById(id, token, NAME_SERVICE);
        if (slot == null) {
            slot = slotRepo.findById(id).orElseThrow();
            redisSlotService.saveToRedis(slot, token, NAME_SERVICE);
        }
        return slot;
    }

    @Override
    @Transactional
    public Slot save(Slot e, String token) {
        boolean isCreate = (e.getSlotId() == null || !slotRepo.existsById(e.getSlotId()));
        e.setUpdateAt(CommonFunction.getCurrentTime());
        e.setUpdateBy(redisSlotService.getCurrentUserId(token));
        if (e.getSlotId() == null || e.getSlotId().isBlank()) {
            e.setCreatedAt(CommonFunction.getCurrentTime());
            e.setCreatedBy(redisSlotService.getCurrentUserId(token));
            e.setSlotId(UUID.randomUUID().toString());
        }

        e.setSlotId(slotRepo.save(e).getSlotId());
        if (e.getSlotAssigns() != null && !e.getSlotAssigns().isEmpty()) {
            e.getSlotAssigns().stream().forEach(sl -> slotAssignService.save(sl, token));
        }

        entityManager.flush();
        entityManager.clear();

        Slot newSlot = new Slot();
        newSlot = slotRepo.findById(e.getSlotId()).orElseThrow();

        redisSlotService.saveToRedis(newSlot, token, NAME_SERVICE);

        kafkaSlotProducer.sendMessage(newSlot, isCreate ? KAFKA_TOPIC_CREATE : KAFKA_TOPIC_UPDATE);

        messagingTemplate.convertAndSend("/topic/task-service", newSlot);

        return newSlot;
    }

    public List<Slot> getAllByGroupId(String token, String groupId) {
        List<Slot> slots = redisSlotService.getAll(token, NAME_SERVICE);
        if (slots.isEmpty()) {
            slots = slotRepo.findByGroupId(groupId);
            redisSlotService.saveListToRedis(slots, token, NAME_SERVICE);
        }
        return slots;
    }

    public List<Slot> getAllBySemesterId(String semesterId, String token) {
        List<String> groupIds = classServiceClient.getGroupssBySemesterId(token, semesterId)
                .stream()
                .map(Groupss::getGroupsId)
                .filter(id -> id != null && !id.isBlank())
                .toList();

        if (groupIds.isEmpty())
            return List.of();

        String currentUserId = redisSlotService.getCurrentUserId(token);

        List<Slot> personalSlots = slotRepo.findAllByGroupIds(groupIds, currentUserId);
        slotRepo.findAllByCreatedBy(currentUserId).stream().forEach(s -> {
            if (!personalSlots.contains(s)) {
                personalSlots.add(s);
            }
        });

        for (String gid : groupIds) {
            String suffix = "group:" + gid;
            redisSlotService.saveListToRedisWithSuffix(
                    personalSlots.stream().filter(t -> gid.equals(t.getGroupId())).toList(),
                    token, NAME_SERVICE, suffix);
        }
        // 5) cũng có thể fill cache tổng theo user nếu cần
        redisSlotService.saveListToRedis(personalSlots, token, NAME_SERVICE);

        return personalSlots;

    }

}
