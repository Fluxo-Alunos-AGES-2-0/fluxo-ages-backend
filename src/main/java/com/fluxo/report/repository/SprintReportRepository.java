package com.fluxo.report.repository;

import com.fluxo.report.entity.SprintReport;
import com.fluxo.report.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SprintReportRepository extends JpaRepository<SprintReport, Integer> {

    SprintReport findSprintReportById(Integer reportId);

    Optional<SprintReport> findByStudentUserIdAndProjectIdAndSprintAndType(
            Integer studentUserId,
            Integer projectId,
            String sprint,
            ReportType type
    );

    Optional<SprintReport> findByStudentUserIdAndProjectIdAndSprint(
            Integer studentUserId,
            Integer projectId,
            String sprint
    );

    List<SprintReport> findByStudentUserIdAndType(
            Integer studentUserId,
            ReportType type
    );

    List<SprintReport> findByStudentUserId(Integer studentUserId);

    List<SprintReport> findByStudentUserIdAndProjectIdAndType(
            Integer studentUserId,
            Integer projectId,
            ReportType type
    );

    List<SprintReport> findByStudentUserIdAndProjectId(
            Integer studentUserId,
            Integer projectId
    );

    @Modifying
    @Query(value = "DELETE FROM sprint_report WHERE id_report = :idReport", nativeQuery = true)
    int deleteChildByReportId(@Param("idReport") Integer idReport);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM sprint_report WHERE id_report = :idReport)", nativeQuery = true)
    boolean existsChildByReportId(@Param("idReport") Integer idReport);
}