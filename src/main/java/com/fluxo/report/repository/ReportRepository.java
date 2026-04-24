package com.fluxo.report.repository;

import com.fluxo.report.dto.ProgressReportResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository {
    @Query("""
                SELECT new com.fluxo.report.dto.FinalReportResponseDto(
                    r.createDate,
                    p.name,
                    eb.grade,
                    rr.comment
                )
                FROM Report r
                JOIN r.studentUser u
                JOIN StudentProfile sp ON sp.studentUser.id = u.id
                JOIN Team t ON t.id = sp.team.id
                JOIN Project p ON p.id = t.project.id
                LEFT JOIN ReportReview rr ON rr.report.id = r.id
                LEFT JOIN ExaminingBoard eb ON eb.studentUser.id = u.id
                WHERE u.id = :userId
            """)
    List<ProgressReportResponseDto> findProgressReportsByUserId(Integer userId);
}
