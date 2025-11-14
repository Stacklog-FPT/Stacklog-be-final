package com.stacklog.task_service.model.service;

import java.time.LocalDateTime;
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
import com.stacklog.task_service.model.entities.Task;
import com.stacklog.task_service.model.repo.TaskRepo;

import jakarta.persistence.EntityManager;

@Service
public class TaskService implements IService<Task> {

    private static final String NAME_SERVICE = "task-service";
    private static final String GROUP_SUFFIX_PREFIX = "group:";

    private static final String KAFKA_TOPIC_UPDATE = "task-service.task.updated";
    private static final String KAFKA_TOPIC_CREATE = "task-service.task.created";

    @Autowired
    private TaskRepo taskRepo;
    @Autowired
    private TaskAssignService taskAssignService;
    @Autowired
    private CheckListService checkListService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private KafkaProducer<Task> kafkaTaskProducer;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ClassServiceClient classServiceClient;

    @Autowired
    EntityManager entityManager;

    private final RedisService<Task> redisTaskService;

    public TaskService(RedisService<Task> redisTaskService) {
        this.redisTaskService = redisTaskService;
    }

    @Override
    public Task delete(String id, String token) {
        Task task = taskRepo.findById(id).orElseThrow();
        String userId = redisTaskService.getCurrentUserId(token);

        taskRepo.deleteById(id);

        // Rebuild cache theo user
        List<Task> byUser = taskRepo.findByUserId(userId);
        redisTaskService.saveListToRedis(byUser, token, NAME_SERVICE);

        // Nếu có group, rebuild index theo group
        if (task.getGroupId() != null) {
            String suffix = GROUP_SUFFIX_PREFIX + task.getGroupId();
            List<Task> byGroup = taskRepo.findByGroupId(task.getGroupId());
            redisTaskService.saveListToRedisWithSuffix(byGroup, token, NAME_SERVICE, suffix);
        }
        return task;
    }

    @Override
    public List<Task> getAllByUserId(String token) {
        List<Task> tasks = redisTaskService.getAll(token, NAME_SERVICE);
        if (tasks.isEmpty()) {
            String userId = redisTaskService.getCurrentUserId(token);
            tasks = taskRepo.findByUserId(userId);
            redisTaskService.saveListToRedis(tasks, token, NAME_SERVICE);
        }
        return tasks;
    }

    public List<Task> getAllByUserIdAndSemesterId(String token, String semesterId) {
        // 1) gọi class-service để lấy groupIds theo học kỳ
        List<String> groupIds = classServiceClient.getGroupssBySemesterId(token, semesterId)
                .stream()
                .map(Groupss::getGroupsId)
                .filter(id -> id != null && !id.isBlank())
                .toList();

        if (groupIds.isEmpty())
            return List.of();

        // 2) lấy userId hiện tại từ Redis (như bạn đang làm)
        String userId = redisTaskService.getCurrentUserId(token);

        // 3) query DB 1 lần cho tất cả group
        List<Task> tasks = taskRepo.findUserTasksByGroupIds(userId, groupIds);

        // 4) warm cache theo group để lần sau nhanh hơn
        for (String gid : groupIds) {
            String suffix = "group:" + gid;
            redisTaskService.saveListToRedisWithSuffix(
                    tasks.stream().filter(t -> gid.equals(t.getGroupId())).toList(),
                    token, "task-service", suffix);
        }
        // 5) cũng có thể fill cache tổng theo user nếu cần
        redisTaskService.saveListToRedis(tasks, token, "task-service");

        return tasks;
    }

    public List<Task> getAllByGroupId(String token, String groupId) {
        String suffix = GROUP_SUFFIX_PREFIX + groupId;

        List<Task> tasks = redisTaskService.getAllBySuffix(token, NAME_SERVICE, suffix);
        if (tasks.isEmpty()) {
            tasks = taskRepo.findByGroupId(groupId);
            redisTaskService.saveListToRedisWithSuffix(tasks, token, NAME_SERVICE, suffix);
        }
        return tasks;
    }

    @Override
    public Task getById(String id, String token) {
        Task task = taskRepo.findById(id).orElse(null);
        if (task != null) {
            redisTaskService.saveToRedis(task, token, NAME_SERVICE);
            if (task.getGroupId() != null) {
                String suffix = GROUP_SUFFIX_PREFIX + task.getGroupId();
                redisTaskService.saveToRedisWithSuffix(task, token, NAME_SERVICE, suffix);
            }
        }
        return task;
    }

    @Override
    @Transactional
    public Task save(Task e, String token) {
        boolean isCreate = (e.getTaskId() == null || !taskRepo.existsById(e.getTaskId()));
        e = saveToDB(e, token, isCreate);

        if (e.getAssigns() != null && !e.getAssigns().isEmpty()) {
            if (!isCreate) {
                taskAssignService.deleteTaskAssigns(e.getTaskId(), e.getAssigns());
            }
            e.getAssigns().stream().forEach(a -> taskAssignService.save(a, token));
        }
        if (e.getReviews() != null && !e.getReviews().isEmpty()) {
            if (!isCreate) {
                reviewService.deleteReviews(e.getTaskId(), e.getReviews());
            }
            e.getReviews().stream().forEach(r -> reviewService.save(r, token));
        }
        if (e.getCheckLists() != null && !e.getCheckLists().isEmpty()) {
            if (!isCreate) {
                checkListService.deleteCheckLists(e.getTaskId(), e.getCheckLists());
            }
            e.getCheckLists().stream().forEach(cl -> checkListService.save(cl, token));
        }
        if (e.getSubtasks() != null && !e.getSubtasks().isEmpty()) {
            e.getSubtasks().stream().forEach(
                    st -> saveToDB(st, token, (st.getTaskId() == null || !taskRepo.existsById(st.getTaskId()))));
        }

        entityManager.flush();
        entityManager.clear();

        Task newTask = new Task();
        if (e.getParentTask() == null) {
            newTask = taskRepo.findById(e.getTaskId()).orElse(null);
        } else {
            newTask = taskRepo.findById(e.getParentTask().getTaskId()).orElse(null);
        }

        // Cập nhật cache index tổng theo user
        redisTaskService.saveToRedis(newTask, token, NAME_SERVICE);

        // Nếu có group → cập nhật index theo group
        if (e.getGroupId() != null) {
            String suffix = GROUP_SUFFIX_PREFIX + newTask.getGroupId();
            redisTaskService.saveToRedisWithSuffix(newTask, token, NAME_SERVICE, suffix);
        }

        kafkaTaskProducer.sendMessage(newTask, isCreate ? KAFKA_TOPIC_CREATE : KAFKA_TOPIC_UPDATE);
        messagingTemplate.convertAndSend("/topic/task-service", e);
        return newTask;
    }

    @Transactional
    private Task saveToDB(Task e, String token, boolean isCreate) {
        LocalDateTime now = CommonFunction.getCurrentTime();
        String currentUserId = redisTaskService.getCurrentUserId(token);

        e.setUpdateAt(now);
        e.setUpdateBy(currentUserId);
        if (isCreate) {
            e.setCreatedAt(now);
            e.setCreatedBy(currentUserId);
            e.setTaskId(UUID.randomUUID().toString());
        }

        e.setTaskId(taskRepo.save(e).getTaskId());
        return e;
    }
}
