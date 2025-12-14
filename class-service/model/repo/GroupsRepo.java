package com.stacklog.class_service.model.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stacklog.class_service.model.entities.Groupss;

@Repository
public interface GroupsRepo extends JpaRepository<Groupss, String> {

    @Query("SELECT gs.groups FROM GroupStudent gs WHERE gs.userId = :userId")
    List<Groupss> findByUserId(@Param("userId") String userId);

    List<Groupss> findByClassesClassesId(String classId);

    @Query(value = """
            SELECT g.*
            FROM group_student gs
            JOIN groupss g ON gs.groups_id = g.groups_id
            JOIN classes c ON g.classes_id = c.classes_id
            JOIN semester s ON c.semester_id = s.semester_id
            WHERE s.semester_id = :semesterId
              AND gs.user_id = :userId
            """, nativeQuery = true)
    List<Groupss> findBySemesterIdAndUserId(@Param("semesterId") String semesterId, @Param("userId") String userId);

}