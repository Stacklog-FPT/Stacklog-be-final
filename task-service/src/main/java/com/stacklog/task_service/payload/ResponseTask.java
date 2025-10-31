package com.stacklog.task_service.payload;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.stacklog.task_service.model.entities.CheckList;
import com.stacklog.task_service.model.entities.Review;
import com.stacklog.task_service.model.entities.Task;
import com.stacklog.task_service.model.entities.TaskAssign;
import com.stacklog.task_service.model.entities.Task.Priority;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseTask {
    private String taskId;
    private String taskTitle;
    private String taskDescription;
    private String groupId;
    private String documentId;
    private Integer taskPoint;
    private LocalDateTime taskStartTime;
    private LocalDateTime taskDueDate;
    private Priority priority;
    private List<Task> subtasks;
    private List<Review> reviews;
    private String statusTaskId;
    private List<CheckList> checkLists;
    private List<String> assignTo;

    public ResponseTask(Task task) {
        this.taskId = task.getTaskId();
        this.taskTitle = task.getTaskTitle();
        this.taskDescription = task.getTaskDescription();
        this.groupId = task.getGroupId();
        this.documentId = task.getDocumentId();
        this.taskPoint = task.getTaskPoint();
        this.taskStartTime = task.getTaskStartTime();
        this.taskDueDate = task.getTaskDueDate();
        this.priority = task.getPriority();
        this.subtasks = task.getSubtasks();
        this.reviews = task.getReviews();
        this.statusTaskId = task.getStatusTask().getStatusTaskId();
        this.checkLists = task.getCheckLists();
        this.assignTo = convertAssignsToAssignTo(task.getAssigns());
    }

    private List<String> convertAssignsToAssignTo(List<TaskAssign> taskAssigns) {
        List<String> assignTo = new ArrayList<>();
        if (taskAssigns == null || taskAssigns.isEmpty()) {
            return new ArrayList<>();
        }
        taskAssigns.stream().forEach(ta -> assignTo.add(ta.getAssignTo()));
        return assignTo;
    }

}

