package com.stacklog.task_service.model.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stacklog.task_service.model.entities.StatusTask;

@Repository
public interface StatusTaskRepo extends JpaRepository<StatusTask, String> {

    List<StatusTask> findAllByGroupId(String groupId);

    List<StatusTask> findByCreatedBy(String userId);
    
}
