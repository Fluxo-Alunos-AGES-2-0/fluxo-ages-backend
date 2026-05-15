package com.fluxo.user.repository;

import com.fluxo.user.entity.StudentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentHistoryRepository extends JpaRepository<StudentHistory, Integer> {
    List<StudentHistory> findByStudentUserId(Integer userId);
}
