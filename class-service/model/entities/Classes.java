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
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class Classes extends CoreEntity {

    @Id
    private String classesId;

    private String classesName;
    private String lectureId;

    @OneToMany(mappedBy = "classes", cascade = CascadeType.REMOVE)
    @JsonManagedReference
    private List<Groupss> groups;

    @ManyToOne
    @JoinColumn(name = "semesterId")
    @JsonBackReference
    private Semester semester;
}