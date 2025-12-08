package com.stacklog.class_service.model.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stacklog.class_service.model.entities.Semester;

@Repository
public interface SemesterRepo extends JpaRepository<Semester, String> {

    @Query("""
                SELECT DISTINCT s
                FROM Semester s
                JOIN s.classes c
                JOIN c.groups g
                JOIN g.groupStudents gs
                WHERE gs.userId = :userId
            """)
    List<Semester> findAllByMemberUserId(@Param("userId") String userId);

    @Query("""
                SELECT DISTINCT s
                FROM Semester s
                JOIN s.classes c
                JOIN c.groups g
                JOIN g.groupStudents gs
                WHERE gs.userId = :userId
                  AND s.semesterYear = :year
                  AND s.quarter = :quarter
            """)
    List<Semester> findAllByYearQuarterAndUser(
            @Param("year") Integer year,
            @Param("quarter") Semester.Quarter quarter,
            @Param("userId") String userId);

}