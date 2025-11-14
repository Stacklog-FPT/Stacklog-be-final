package com.stacklog.task_service.model.repo;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stacklog.task_service.model.entities.TaskAssign;

@Repository
public interface TaskAssignRepo extends JpaRepository<TaskAssign, String> {

        public List<TaskAssign> findByAssignTo(String assignTo);

        public List<TaskAssign> findByTaskTaskId(String taskId);

        TaskAssign findByTaskTaskIdAndAssignTo(@Param("taskId") String taskId,
                        @Param("assignTo") String assignTo);

        @Modifying
        @Query("delete from TaskAssign ta where ta.task.taskId = :taskId")
        public void deleteAllByTaskId(String taskId);

        @Modifying
        @Query("delete from TaskAssign ta where ta.task.taskId = :taskId and ta.assignTo not in :keepIds")
        public void deleteAllNotIn(String taskId, Set<String> keepIds);

}
