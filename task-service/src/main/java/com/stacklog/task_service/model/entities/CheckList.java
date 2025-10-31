package com.stacklog.task_service.model.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.stacklog.core_service.model.entities.CoreEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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
public class CheckList extends CoreEntity {

    @Id
    private String checkListId;

    private String checkListName;

    @OneToMany(mappedBy = "checkList", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference("checklists-checkItems")
    private List<CheckItem> listItems;

    @ManyToOne
    @JoinColumn(name = "taskId")
    @JsonBackReference("task-checkLists")
    private Task task;

    public CheckList(String createdBy, String createdAt, String updateBy, String updateAt,
            String checkListName, List<CheckItem> listItems, Task task) {
        super(createdBy, createdAt, updateBy, updateAt);
        this.checkListName = checkListName;
        this.listItems = listItems;
        this.task = task;
    }

    public CheckList() {
    }

}
