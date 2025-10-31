package com.stacklog.task_service.model.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.stacklog.core_service.model.entities.CoreEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class TaskAssign extends CoreEntity {

    @Id
    private String taskAssignId;

    @ManyToOne
    @JoinColumn(name = "taskId")
    @JsonBackReference("task-assigns")
    private Task task;

    private String assignTo;

    public TaskAssign(String createdBy, String createdAt, String updateBy, String updateAt, Task task,
            String assignTo) {
        super(createdBy, createdAt, updateBy, updateAt);
        this.task = task;
        this.assignTo = assignTo;
    }

    public TaskAssign() {
    }

}