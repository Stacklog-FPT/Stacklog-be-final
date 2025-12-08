package com.stacklog.class_service.model.entities;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.stacklog.core_service.model.entities.CoreEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class Semester extends CoreEntity {

    @Id
    private String semesterId;

    private String semesterName;
    private Integer semesterYear;
    private LocalDate semesterStartDate;
    private LocalDate semesterEndDate;

    @Enumerated(EnumType.STRING)
    private Quarter quarter;
    
    public enum Quarter {
        SP, SU, FA
    }

    @OneToMany(mappedBy = "semester")
    @JsonManagedReference
    @JsonIgnore
    private List<Classes> classes;

}