package com.stacklog.schedule_service.model.entities;

import com.stacklog.core_service.model.entities.CoreEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class SlotAssign extends CoreEntity {

    @Id
    private String slotAssignId;

    private String userId;

    @ManyToOne
    @JoinColumn(name = "slotId")
    @JsonBackReference("slot-assign")
    private Slot slot;

}
