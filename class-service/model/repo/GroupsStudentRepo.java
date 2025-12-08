package com.stacklog.class_service.model.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stacklog.class_service.model.entities.GroupStudent;

@Repository
public interface GroupsStudentRepo extends JpaRepository<GroupStudent, String> {

    List<GroupStudent> findByUserId(String userId);

    Optional<GroupStudent> findByGroupsGroupsIdAndUserId(String groupId, String userId);

    @Query("select g.groupStudents from Groupss g where g.groupsId = :groupId")
    List<GroupStudent> findAllByGroupGroupId(@Param("groupId") String groupId);

    Optional<GroupStudent> findByGroupsClassesClassesIdAndUserId(String classId, String studentId);
    
}