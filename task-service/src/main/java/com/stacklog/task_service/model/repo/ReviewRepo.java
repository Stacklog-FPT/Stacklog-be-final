package com.stacklog.task_service.model.repo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stacklog.task_service.model.entities.Review;

@Repository
public interface ReviewRepo extends JpaRepository<Review, String> {

    @Query("SELECT r FROM Review r WHERE r.task.taskId = :taskId")
    List<Review> findByTaskId(@Param("taskId") String taskId);

    @Modifying
    @Query("delete from Review r where r.task.taskId = :taskId and r.reviewId not in :keepIds")
    int deleteAllNotIn(@Param("taskId") String taskId, @Param("keepIds") Collection<String> keepIds);

    @Modifying
    @Query("delete from Review r where r.task.taskId = :taskId")
    void deleteAllByTaskId(String taskId);

}
