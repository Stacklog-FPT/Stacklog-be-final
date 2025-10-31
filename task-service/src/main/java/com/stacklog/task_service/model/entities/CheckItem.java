package com.stacklog.task_service.model.entities;

import java.time.LocalDateTime;

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
public class CheckItem extends CoreEntity {

    @Id
    private String checkItemId;

    private String checkItemTitle;
    private String checkItemDescription;
    private LocalDateTime checkItemDueDate;
    private Boolean isChecked;

    @ManyToOne
    @JoinColumn(name = "checkListId")
    @JsonBackReference("checklists-checkItems")
    private CheckList checkList;

    public CheckItem(String createdBy, String createdAt, String updateBy, String updateAt,
            String checkItemTitle, String checkItemDescription, String checkItemDueDate, Boolean isChecked) {
        super(createdBy, createdAt, updateBy, updateAt);
        this.checkItemTitle = checkItemTitle;
        this.checkItemDescription = checkItemDescription;
        this.isChecked = isChecked;
        try {
            this.checkItemDueDate = super.convertTime(checkItemDueDate);
        } catch (Exception e) {
            System.out.println(e);
            this.checkItemDueDate = null;
        }
    }

    public CheckItem() {
    }

}
