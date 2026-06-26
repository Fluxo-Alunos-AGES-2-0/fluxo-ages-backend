package com.fluxo.user.repository;

import com.fluxo.project.entity.Project;
import com.fluxo.user.entity.StudentHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface StudentHistoryRepository extends JpaRepository<StudentHistory, Integer> {
    List<StudentHistory> findByStudentUserId(Integer userId);

    @Query("""
            SELECT DISTINCT sh
            FROM StudentHistory sh
            JOIN FETCH sh.project p
            LEFT JOIN FETCH p.technologies
            WHERE sh.studentUser.id = :userId
            ORDER BY sh.semesterYear DESC
            """)
    List<StudentHistory> findByStudentUserIdOrderByRecent(Integer userId);
    
    List<StudentHistory> findByProject(Project project);

    Optional<StudentHistory> findFirstByStudentUserIdAndProjectIdOrderBySemesterYearDesc(
            Integer userId,
            Integer projectId
    );
}
