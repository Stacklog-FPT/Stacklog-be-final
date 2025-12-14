package com.stacklog.class_service.model.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.stacklog.core_service.model.entities.CoreEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class GroupStudent extends CoreEntity {
    
    @Id
    private String groupStudentId;

    private String userId;

    @ManyToOne
    @JoinColumn(name = "groupsId")
    @JsonBackReference
    private Groupss groups;

}