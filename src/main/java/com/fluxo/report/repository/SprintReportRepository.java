package com.fluxo.report.repository;

import com.fluxo.report.entity.SprintReport;
import com.fluxo.report.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SprintReportRepository extends JpaRepository<SprintReport, Integer> {

    Optional<SprintReport> findByStudentUserIdAndProjectIdAndSprintAndType(
            Integer studentUserId,
            Integer projectId,
            String sprint,
            ReportType type
    );
}