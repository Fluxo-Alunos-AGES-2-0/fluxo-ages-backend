package com.fluxo.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fluxo.report.entity.ReportArchive;

import java.util.Optional;

public interface ReportArchiveRepository extends JpaRepository<ReportArchive, Integer> {
    Optional<ReportArchive> findByStudentUserIdAndType(Integer studentId, Integer type);
    Optional<ReportArchive> findByStudentUserIdAndProjectIdAndType(Integer studentId, Integer projectId, Integer type);
}