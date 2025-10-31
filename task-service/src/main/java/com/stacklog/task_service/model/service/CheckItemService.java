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
import com.stacklog.task_service.model.entities.CheckItem;
import com.stacklog.task_service.model.repo.CheckItemRepo;

@Service
public class CheckItemService implements IService<CheckItem> {

    private static final String NAME_SERVICE = "task-service";

    private static final String KAFKA_TOPIC_UPDATE = "task-service.checkitem.updated";
    private static final String KAFKA_TOPIC_CREATE = "task-service.checkitem.created";

    @Autowired CheckItemRepo checkItemRepo;

    @Autowired
    KafkaProducer<CheckItem> checkItemProducer;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    RedisService<CheckItem> redisCheckItemService;

    public CheckItemService(RedisService<CheckItem> redisCheckItemService) {
        this.redisCheckItemService = redisCheckItemService;
    }

    @Override
    public CheckItem delete(String id, String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public List<CheckItem> getAllByUserId(String token) {
        List<CheckItem> checkItems = redisCheckItemService.getAll(token, NAME_SERVICE);
        if (checkItems.isEmpty() || checkItems == null) {
            checkItems = checkItemRepo.findAllByUserId(redisCheckItemService.getCurrentUserId(token));
            redisCheckItemService.saveListToRedis(checkItems, token, NAME_SERVICE);
        }
        return checkItems;
    }

    @Override
    public CheckItem getById(String id, String token) {
        CheckItem checkItem = redisCheckItemService.getById(id, token, NAME_SERVICE);
        if (checkItem == null) {
            checkItem = checkItemRepo.findById(id).orElseThrow();
            redisCheckItemService.saveToRedis(checkItem, token, NAME_SERVICE);
        }
        return checkItem;
    }

    @Override
    @Transactional
    public CheckItem save(CheckItem e, String token) {
        boolean isCreate = (e.getCheckItemId() == null || !checkItemRepo.existsById(e.getCheckItemId()));
        e.setUpdateAt(CommonFunction.getCurrentTime());
        e.setUpdateBy(redisCheckItemService.getCurrentUserId(token));
        if (isCreate) {
            e.setCreatedAt(CommonFunction.getCurrentTime());
            e.setCreatedBy(redisCheckItemService.getCurrentUserId(token));
            e.setCheckItemId(UUID.randomUUID().toString());
        }

        e.setCheckItemId(checkItemRepo.save(e).getCheckItemId());

        CheckItem newCheckItem = checkItemRepo.findById(e.getCheckItemId()).orElseThrow();

        redisCheckItemService.saveToRedis(newCheckItem, token, NAME_SERVICE);

        checkItemProducer.sendMessage(newCheckItem, isCreate ? KAFKA_TOPIC_CREATE : KAFKA_TOPIC_UPDATE);

        messagingTemplate.convertAndSend("/topic/task-service", e);

        return e;
    }

    public void deleteCheckItems(String checkListId, List<CheckItem> checkItemsFE) {
        Set<String> keepIds = checkItemsFE.stream()
                .map(CheckItem::getCheckItemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (keepIds == null || keepIds.isEmpty()) {
            checkItemRepo.deleteAllByCheckListId(checkListId);
            return;
        } 
        checkItemRepo.deleteAllNotIn(checkListId, keepIds);
    }
}
