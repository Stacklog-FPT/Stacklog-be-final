package com.stacklog.class_service.model.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stacklog.class_service.model.entities.Classes;

@Repository
public interface ClassesRepo extends JpaRepository<Classes, String> {

  @Query("""
        SELECT DISTINCT c
        FROM GroupStudent gs
        JOIN gs.groups g
        JOIN g.classes c
        JOIN c.semester s
        WHERE gs.userId = :currentUserId
          AND s.semesterId = :semesterId
      """)
  List<Classes> findAllBySemesterIdNUserId(
      @Param("currentUserId") String currentUserId,
      @Param("semesterId") String semesterId);

  List<Classes> findAllByLectureIdAndSemesterSemesterId(String lectureId, String semesterId);

  List<Classes> findAllBySemesterSemesterId(String semesterId);

}