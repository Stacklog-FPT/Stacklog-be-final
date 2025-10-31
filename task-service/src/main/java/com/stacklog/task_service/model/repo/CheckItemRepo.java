package com.stacklog.task_service.model.repo;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stacklog.task_service.model.entities.CheckItem;

@Repository
public interface CheckItemRepo extends JpaRepository<CheckItem, String> {

    @Query(value = "SELECT ci.* FROM check_item ci " +
    "JOIN check_list cl ON ci.check_list_id = cl.check_list_id " +
    "JOIN task t ON cl.task_id = t.task_id " +
    "JOIN task_assign ta ON ta.task_id = t.task_id " +
    "WHERE ta.assign_to = :userId", nativeQuery = true)
    List<CheckItem> findAllByUserId(@Param("userId") String currentUserId);

    @Modifying
    @Query("delete from CheckItem ct where ct.checkList.checkListId = :checkListId")
    public void deleteAllByCheckListId(String checkListId);

    @Modifying
    @Query("delete from CheckItem ct where ct.checkList.checkListId = :checkListId and ct.checkItemId not in :keepIds")
    public void deleteAllNotIn(String checkListId, Set<String> keepIds);

}
