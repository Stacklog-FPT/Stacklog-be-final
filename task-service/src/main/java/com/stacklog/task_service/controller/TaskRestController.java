package com.stacklog.task_service.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stacklog.task_service.model.entities.CheckItem;
import com.stacklog.task_service.model.entities.CheckList;
import com.stacklog.task_service.model.entities.Review;
import com.stacklog.task_service.model.entities.Task;
import com.stacklog.task_service.model.entities.TaskAssign;
import com.stacklog.task_service.model.entities.Task.Priority;
import com.stacklog.task_service.model.service.StatusTaskService;
import com.stacklog.task_service.model.service.TaskAssignService;
import com.stacklog.task_service.model.service.TaskService;
import com.stacklog.task_service.payload.ResponseTask;

import lombok.Getter;
import lombok.Setter;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(path = "/task")
public class TaskRestController {

    @Autowired
    TaskService taskService;

    @Autowired
    TaskAssignService taskAssignService;

    @Autowired
    StatusTaskService statusTaskService;

    @GetMapping("/{groupId}")
    public ResponseEntity<List<ResponseTask>> getTasksByGroupId(@RequestHeader("Authorization") String token,
            @PathVariable("groupId") String groupId) {
        List<ResponseTask> lists = new ArrayList<>();
        taskService.getAllByGroupId(token, groupId).stream().forEach(t -> lists.add(new ResponseTask(t)));

        if (lists.isEmpty() || lists == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(lists);
    }

    @GetMapping("/personal-task")
    public ResponseEntity<Map<String, List<ResponseTask>>> getTasksPersonal(
            @RequestHeader("Authorization") String token,
            @RequestParam(name = "semesterId", required = false) String semesterId) {

        List<Task> tasks = (semesterId != null && !semesterId.isBlank())
                ? taskService.getAllByUserIdAndSemesterId(token, semesterId)
                : taskService.getAllByUserId(token);

        Map<String, List<ResponseTask>> result = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> Optional.ofNullable(t.getStatusTask())
                                .map(st -> st.getStatusTaskName())
                                .map(String::trim)
                                .map(String::toUpperCase)
                                .orElse("UNKNOWN"),
                        LinkedHashMap::new,
                        Collectors.mapping(ResponseTask::new, Collectors.toList())));

        return ResponseEntity.ok(result);
    }

    @PostMapping("/save")
    public ResponseEntity<ResponseTask> saveTask(@RequestHeader("Authorization") String token, @RequestBody TaskDTO e) {
        Task task = new Task();
        System.out.println(e.toString());
        if (e.getTaskId() != null || !e.getTaskId().isBlank()) {
            task.setTaskId(e.getTaskId());
        }
        task.setTaskTitle(e.getTaskTitle());
        task.setTaskDescription(e.getTaskDescription());
        task.setGroupId(e.getGroupId());
        task.setDocumentId(e.getDocumentId());
        task.setTaskPoint(e.getTaskPoint());
        task.setTaskStartTime(e.getTaskStartTime());
        task.setTaskDueDate(e.getTaskDueDate());
        task.setPriority(e.getPriority());
        task.setStatusTask(statusTaskService.getById(e.getStatusTaskId(), token));
        task.setParentTask(null);
        List<TaskAssign> assigns = new ArrayList<>();
        if (e.getListUserAssign() != null && !e.getListUserAssign().isEmpty()) {
            for (String userId : e.getListUserAssign()) {
                TaskAssign taskAssign = new TaskAssign();
                taskAssign.setAssignTo(userId);
                taskAssign.setTask(task);
                assigns.add(taskAssign);
            }
        }
        for (Review review : e.getReviews()) {
            review.setTask(task);
        }
        for (CheckList checkList : e.getCheckLists()) {
            checkList.setTask(task);
            for (CheckItem checkItem : checkList.getListItems()) {
                checkItem.setCheckList(checkList);
            }
        }
        task.setAssigns(assigns);
        task.setCheckLists(e.getCheckLists());
        task.setReviews(e.getReviews());
        task.setSubtasks(e.getSubTasks());
        task = taskService.save(task, token);
        

        if (task == null) {
            return ResponseEntity.badRequest().build();
        }
        task = taskService.getById(task.getTaskId(), token);
        return ResponseEntity.ok().body(new ResponseTask(task));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<String> deleteTask(@RequestHeader("Authorization") String token,
            @PathVariable("taskId") String taskId) {
        Task task = taskService.delete(taskId, token);
        if (task == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body("Delete success");
    }

    @PostMapping("/subtask/save")
    public ResponseEntity<ResponseTask> saveSubTask(@RequestHeader("Authorization") String token, @RequestBody SubTaskDTO e) {
        Task task = new Task();
        if (e.getTaskId() != null || !e.getTaskId().isBlank()) {
            task.setTaskId(e.getTaskId());
        }
        task.setTaskTitle(e.getTaskTitle());
        task.setTaskDescription(e.getTaskDescription());
        task.setGroupId(e.getGroupId());
        task.setDocumentId(e.getDocumentId());
        task.setTaskPoint(e.getTaskPoint());
        task.setTaskStartTime(e.getTaskStartTime());
        task.setTaskDueDate(e.getTaskDueDate());
        task.setPriority(e.getPriority());
        task.setStatusTask(statusTaskService.getById(e.getStatusTaskId(), token));
        if (e.getParentTaskId() != null && !e.getParentTaskId().isBlank()) {
            Task parent = taskService.getById(e.getParentTaskId(), token);
            task.setParentTask(parent);
        } else {
            return ResponseEntity.status(459).body(new ResponseTask(task));
        }
        List<TaskAssign> assigns = new ArrayList<>();
        if (e.getListUserAssign() != null && !e.getListUserAssign().isEmpty()) {
            for (String userId : e.getListUserAssign()) {
                TaskAssign taskAssign = new TaskAssign();
                taskAssign.setAssignTo(userId);
                taskAssign.setTask(task);
                assigns.add(taskAssign);
            }
        }
        task = taskService.save(task, token);
        if (task == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(new ResponseTask(task));
    }
    

}

@Getter
@Setter
class TaskDTO {
    private String taskId;
    private String taskTitle;
    private String taskDescription;
    private String groupId;
    private String documentId;
    private Integer taskPoint;
    private LocalDateTime taskStartTime;
    private LocalDateTime taskDueDate;
    private Priority priority;
    private String statusTaskId;
    private List<String> listUserAssign;
    private List<Review> reviews;
    private List<CheckList> checkLists;
    private List<Task> subTasks;
}

@Getter
@Setter
class SubTaskDTO {
    private String taskId;
    private String taskTitle;
    private String taskDescription;
    private String groupId;
    private String documentId;
    private Integer taskPoint;
    private LocalDateTime taskStartTime;
    private LocalDateTime taskDueDate;
    private Priority priority;
    private String statusTaskId;
    private List<String> listUserAssign;
    private String parentTaskId;
}

