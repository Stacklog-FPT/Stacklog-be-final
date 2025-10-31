package com.stacklog.task_service.model.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stacklog.core_service.model.entities.CoreEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class StatusTask extends CoreEntity {

    @Id
    private String statusTaskId;

    private String statusTaskName;
    private String statusTaskColor;
    private String groupId;

    @OneToMany(mappedBy = "statusTask", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Task> tasks;

    public StatusTask(String createdBy, String createdAt, String updateBy, String updateAt,
            String statusTaskName, String statusTaskColor) {
        super(createdBy, createdAt, updateBy, updateAt);
        this.statusTaskName = statusTaskName;
        this.statusTaskColor = statusTaskColor;
    }

    public StatusTask() {
    }

}
