package com.fluxo.report.repository;

import com.fluxo.report.entity.ReportArchive;
import com.fluxo.report.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportArchiveRepository extends JpaRepository<ReportArchive, Integer> {

    Optional<ReportArchive> findByStudentUserIdAndType(Integer studentId, ReportType type);

    Optional<ReportArchive> findByStudentUserIdAndProjectIdAndType(Integer studentId, Integer projectId, ReportType type);

    @Modifying
    @Query(value = "DELETE FROM report_archive WHERE id_report = :idReport", nativeQuery = true)
    int deleteChildByReportId(@Param("idReport") Integer idReport);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM report_archive WHERE id_report = :idReport)", nativeQuery = true)
    boolean existsChildByReportId(@Param("idReport") Integer idReport);
}