package com.stacklog.task_service.model.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.stacklog.core_service.model.entities.CoreEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class Task extends CoreEntity {

    @Id
    private String taskId;

    private String taskTitle;
    private String taskDescription;
    private String groupId;
    private String documentId;
    private Integer taskPoint;
    private LocalDateTime taskStartTime;
    private LocalDateTime taskDueDate;

    @Enumerated(EnumType.STRING) // hoặc EnumType.ORDINAL
    private Priority priority;

    public enum Priority {
        HIGH, MEDIUM, LOW
    }

    @ManyToOne
    @JoinColumn(name = "statusTaskId")
    private StatusTask statusTask;

    @ManyToOne
    @JoinColumn(name = "parentTaskId")
    @JsonBackReference("task-subtasks")
    private Task parentTask;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.REMOVE)
    @JsonManagedReference("task-subtasks")
    private List<Task> subtasks;

    @OneToMany(mappedBy = "task", cascade = CascadeType.REMOVE)
    @JsonManagedReference("task-review")
    private List<Review> reviews;

    @OneToMany(mappedBy = "task", cascade = CascadeType.REMOVE)
    @JsonManagedReference("task-assigns")
    private List<TaskAssign> assigns;

    @OneToMany(mappedBy = "task", cascade = CascadeType.REMOVE)
    @JsonManagedReference("task-checkLists")
    private List<CheckList> checkLists;

    public Task(String createdBy, String createdAt, String updateBy, String updateAt, String taskTitle,
            String taskDescription, String groupId, String documentId, Integer taskPoint, String taskDueDate,
            StatusTask statusTask, Task parentTask, List<Task> subtasks, List<TaskAssign> assigns, List<Review> reviews,
            List<CheckList> checkLists) {
        super(createdBy, createdAt, updateBy, updateAt);
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.groupId = groupId;
        this.documentId = documentId;
        this.taskPoint = taskPoint;
        this.statusTask = statusTask;
        this.parentTask = parentTask;
        this.subtasks = subtasks;
        this.assigns = assigns;
        this.checkLists = checkLists;
        try {
            this.taskDueDate = super.convertTime(taskDueDate);
        } catch (Exception e) {
            System.out.println(e);
            this.taskDueDate = null;
        }
        this.reviews = reviews;

    }

    public Task() {
    }

}
