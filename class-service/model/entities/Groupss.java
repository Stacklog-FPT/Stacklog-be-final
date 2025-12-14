package com.stacklog.class_service.model.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.stacklog.core_service.model.entities.CoreEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Groupss extends CoreEntity {
    
    @Id
    private String groupsId;

    private String groupsName;
    private String groupsDescriptions;
    private Integer groupsMaxMember;
    private Double groupsAvgScore;
    
    private String groupsLeaderId;

    @ManyToOne
    @JoinColumn(name = "classesId")
    @JsonBackReference
    private Classes classes;

    @OneToMany(mappedBy = "groups" , cascade = CascadeType.REMOVE)
    @JsonManagedReference
    private List<GroupStudent> groupStudents;

}