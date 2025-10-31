package com.stacklog.topic_service.model.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stacklog.topic_service.model.entities.ProjectInformation;

@Repository
public interface ProjectInformationRepo extends JpaRepository<ProjectInformation, String> {

    Optional<ProjectInformation> findByGroupId(String groupId);

    @Query("""
            select distinct pi
            from ProjectInformation pi
            WHERE pi.groupId in :groupIds
            """)
    List<ProjectInformation> findAllByGroupIds(@Param("groupIds") List<String> groupIds);

}
