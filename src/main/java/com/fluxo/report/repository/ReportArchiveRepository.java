package com.fluxo.report.repository;

import com.fluxo.report.entity.ReportArchive;
import com.fluxo.report.enums.ReportType; // <-- Não esqueça de importar o Enum!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportArchiveRepository extends JpaRepository<ReportArchive, Integer> {
    Optional<ReportArchive> findByStudentUserIdAndType(Integer studentId, ReportType type);
    Optional<ReportArchive> findByStudentUserIdAndProjectIdAndType(Integer studentId, Integer projectId, ReportType type);
}