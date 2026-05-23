package com.fluxo.report.repository;

import com.fluxo.report.entity.ReportReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportReviewRepository extends JpaRepository<ReportReview, Integer> {

    @Modifying
    @Query(value = "DELETE FROM report_review WHERE id_report = :idReport", nativeQuery = true)
    int deleteChildByReportId(@Param("idReport") Integer idReport);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM report_review WHERE id_report = :idReport)", nativeQuery = true)
    boolean existsChildByReportId(@Param("idReport") Integer idReport);
}