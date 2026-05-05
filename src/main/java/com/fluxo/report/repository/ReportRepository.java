package com.fluxo.report.repository;

import com.fluxo.report.dto.ProgressReportResponseDto;
import com.fluxo.report.entity.Report;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@SpringBootApplication
public interface ReportRepository extends JpaRepository<Report, Integer> {
    List<ProgressReportResponseDto> findProgressReportsByStudentUserId(Integer userId);
}
