package com.fluxo.user.repository;

import com.fluxo.user.entity.StudentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentHistoryRepository extends JpaRepository<StudentHistory, Integer> {
    List<StudentHistory> findByStudentUserId(Integer userId);

    @Query("SELECT sh FROM StudentHistory sh WHERE sh.studentUser.id = :userId ORDER BY sh.semesterYear DESC")
    List<StudentHistory> findByStudentUserIdOrderByRecent(Integer userId);
}
