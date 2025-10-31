package com.stacklog.schedule_service.model.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
public class Slot extends CoreEntity {

    @Id
    private String slotId;

    private String slotTitle;
    private String slotDescription;
    private LocalDateTime slotStartTime;

    private String groupId;

    @OneToMany(mappedBy = "slot", cascade = CascadeType.REMOVE)
    @JsonManagedReference("slot-assign") 
    private List<SlotAssign> slotAssigns;

}