package com.fluxo.report.repository;

import com.fluxo.report.entity.HoursReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoursReportRepository extends JpaRepository<HoursReport, Integer> {

    List<HoursReport> findByStudentUserIdAndProjectId(Integer userId, Integer projectId);

    Optional<HoursReport> findByIdAndStudentUserIdAndProjectId(Integer idReport, Integer userId, Integer projectId);
}