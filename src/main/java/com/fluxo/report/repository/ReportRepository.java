package com.fluxo.report.repository;

import com.fluxo.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Integer> {

    List<Report> findByStudentUserId(Integer userId);

    @Modifying
    @Query(value = "DELETE FROM report WHERE id_report = :idReport", nativeQuery = true)
    int deleteParentByReportId(@Param("idReport") Integer idReport);
}