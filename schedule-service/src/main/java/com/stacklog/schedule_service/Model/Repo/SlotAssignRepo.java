package com.stacklog.schedule_service.model.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stacklog.schedule_service.model.entities.SlotAssign;

@Repository
public interface SlotAssignRepo extends JpaRepository<SlotAssign, String> {

    List<SlotAssign> findByUserId(String currentUserId);

    List<SlotAssign> findBySlotSlotId(String slotId);

    SlotAssign findBySlotSlotIdAndUserId(String slotId, String userId);
    
}
