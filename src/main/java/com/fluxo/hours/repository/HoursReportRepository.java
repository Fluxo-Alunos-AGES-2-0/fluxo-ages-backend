package com.fluxo.hours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fluxo.hours.entity.HoursReport;

import java.util.List;
import java.util.Optional;

public interface HoursReportRepository extends JpaRepository<HoursReport, Integer> {

    boolean existsByStudentUserIdAndExitTimeIsNull(Integer userId);

    Optional<HoursReport> findFirstByStudentUserIdAndExitTimeIsNullOrderByEntryTimeDesc(Integer userId);

    List<HoursReport> findByStudentUserIdAndExitTimeIsNotNullOrderByEntryTimeDesc(Integer userId);

    List<HoursReport> findByStudentUserId(Integer userId);

    List<HoursReport> findByStudentUserIdAndProjectIdAndExitTimeIsNotNullOrderByEntryTimeDesc(Integer userId, Integer projectId);

    @Modifying
    @Query(value = "DELETE FROM hours_report WHERE id_report = :idReport", nativeQuery = true)
    int deleteChildByReportId(@Param("idReport") Integer idReport);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM hours_report WHERE id_report = :idReport)", nativeQuery = true)
    boolean existsChildByReportId(@Param("idReport") Integer idReport);
}
