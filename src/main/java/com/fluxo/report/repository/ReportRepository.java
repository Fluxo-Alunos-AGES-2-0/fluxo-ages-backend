package com.fluxo.report.repository;

import com.fluxo.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Integer> {
    List<Report> findByStudentUserId(Integer userId);
}
