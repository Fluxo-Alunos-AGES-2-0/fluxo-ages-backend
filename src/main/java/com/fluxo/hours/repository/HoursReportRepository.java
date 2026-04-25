package com.fluxo.hours.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fluxo.report.entity.HoursReport;

import java.util.List;
import java.util.Optional;

public interface HoursReportRepository extends JpaRepository<HoursReport, Integer> {

    boolean existsByStudentUserIdAndExitTimeIsNull(Integer userId);

    Optional<HoursReport> findFirstByStudentUserIdAndExitTimeIsNullOrderByEntryTimeDesc(Integer userId);

    List<HoursReport> findByStudentUserIdAndExitTimeIsNotNullOrderByEntryTimeDesc(Integer userId);

    List<HoursReport> findByStudentUserId(Integer userId);
}
