package com.stacklog.topic_service.model.entities;

import java.util.List;

import com.stacklog.core_service.model.entities.CoreEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class ProjectInformation extends CoreEntity {

    @Id
    private String piId;

    private String piTitle;
    private String piAbbreviation;
    private String piDescription;

    @Enumerated(EnumType.STRING)
    private PIStatus piStatus;

    public enum PIStatus {
        ACCEPTED, REJECTED, PENDING, REGISTED
    }

    private String piRejectReason;
    private String piApprovedBy;
    private String piApprovedAt;
    private Boolean isAllowEdit;
    private String groupId;

    private List<String> piDocumentIds;



    
}