package com.stacklog.task_service.model.repo;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.stacklog.task_service.model.entities.CheckList;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface CheckListRepo extends JpaRepository<CheckList, String> {

    @Query(value = "SELECT cl FROM CheckList cl " +
            "JOIN Task t ON cl.task_id = t.task_id " +
            "JOIN TaskAssign ta ON ta.task_id = t.task_id " +
            "WHERE ta.assign_to = :userId ", nativeQuery = true)
    public List<CheckList> findAllByUserId(@Param("userId") String userId);

    @Modifying
    @Query("delete from CheckList cl where cl.task.taskId = :taskId")
    public void deleteAllByTaskId(String taskId);

    @Modifying
    @Query("delete from CheckList cl where cl.task.taskId = :taskId and cl.checkListId not in :keepIds")
    public void deleteAllNotIn(String taskId, Set<String> keepIds);

    public List<CheckList> findByTask_TaskId(String taskId);

}
