package com.stacklog.schedule_service.model.repo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stacklog.schedule_service.model.entities.Slot;

@Repository
public interface SlotRepo extends JpaRepository<Slot, String> {

    @Query(value = """
            SELECT s.*
            FROM slot_assign as sa
            JOIN slot as s ON sa.slot_id = s.slot_id
            WHERE sa.user_id = :currentUserId
            """, nativeQuery = true)
    List<Slot> findByUserId(@Param("currentUserId") String currentUserId);

    List<Slot> findByGroupId(String groupId);

    @Query("""
              select sa.slot
              from SlotAssign sa
              where sa.slot.groupId in :groupIds
                and sa.userId = :currentUserId
            """)
    List<Slot> findAllByGroupIds(@Param("groupIds") List<String> groupIds,
            @Param("currentUserId") String currentUserId);

    Collection<? extends Slot> findAllByCreatedBy(String currentUserId);

}
