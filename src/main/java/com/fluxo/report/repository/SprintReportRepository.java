package com.fluxo.report.repository;

import com.fluxo.report.entity.SprintReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintReportRepository extends JpaRepository<SprintReport, Integer> {
}