package com.stacklog.task_service.model.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stacklog.task_service.model.entities.Task;

@Repository
public interface TaskRepo extends JpaRepository<Task, String> {

    @Query("SELECT ta.task FROM TaskAssign ta WHERE ta.assignTo = :userId OR ta.task.createdBy = :userId")
    public List<Task> findByUserId(@Param("userId") String userId);

    @Query("""
              select distinct t
              from Task t
              left join fetch t.statusTask st
              left join fetch t.subtasks s
              where t.groupId = :groupId
                and t.parentTask is null
            """)
    public List<Task> findByGroupId(@Param("groupId") String groupId);

    @Query("""
            select distinct t
            from Task t
            left join fetch t.statusTask st
            left join fetch t.subtasks s
            where t.parentTask is null
              and t.groupId in :groupIds
              and exists (
                select 1 from TaskAssign ta
                where ta.task = t
                  and ta.assignTo = :userId
              )
            """)
    List<Task> findUserTasksByGroupIds(@Param("userId") String userId,
            @Param("groupIds") List<String> groupIds);

}
