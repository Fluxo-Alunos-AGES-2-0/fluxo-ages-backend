package com.fluxo.report.repository;

import com.fluxo.project.entity.Project;
import com.fluxo.project.entity.ProjectStatus;
import com.fluxo.report.entity.ReportArchive;
import com.fluxo.report.entity.ReportReview;
import com.fluxo.report.enums.ReportType;
import com.fluxo.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "app.storage.s3.bucket=test-bucket",
        "app.storage.s3.region=us-east-2"
})
@ActiveProfiles("test")
@Transactional
class ReportReviewRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ReportReviewRepository reportReviewRepository;

    @Test
    @DisplayName("findById returns a review even when the same report also has an archive row")
    void findByIdReturnsReviewWhenArchiveExistsForSameReport() {
        User teacher = new User();
        teacher.setName("Professor");
        teacher.setEnrollmentNumber("9000000001");
        teacher.setEmail("professor-review@fluxo.com");
        teacher.setPassword("secret");
        teacher.setRole("PROFESSOR");
        entityManager.persist(teacher);

        User student = new User();
        student.setName("Aluno");
        student.setEnrollmentNumber("9000000002");
        student.setEmail("aluno-review@fluxo.com");
        student.setPassword("secret");
        student.setRole("STUDENT");
        entityManager.persist(student);

        Project project = new Project();
        project.setName("Projeto Review");
        project.setDescription("Projeto para validar report review");
        project.setStatus(ProjectStatus.EM_ANDAMENTO);
        project.setPeriod("2026.1");
        project.setTeacherUser(teacher);
        entityManager.persist(project);

        ReportArchive reportArchive = new ReportArchive();
        reportArchive.setType(ReportType.RF);
        reportArchive.setCreateDate(OffsetDateTime.parse("2026-06-26T21:18:38Z"));
        reportArchive.setEditDate(OffsetDateTime.parse("2026-06-26T21:18:38Z"));
        reportArchive.setStudentUser(student);
        reportArchive.setProject(project);
        reportArchive.setUrlArchive("s3:prod/reports/rf/15/sample.pdf");
        entityManager.persist(reportArchive);
        entityManager.flush();

        ReportReview reportReview = new ReportReview();
        reportReview.setReport(reportArchive);
        reportReview.setComment("Feedback do professor");
        reportReview.setCorrectionUrl("s3:prod/reviews/15/correction.pdf");
        reportReview.setRevisionDate(OffsetDateTime.parse("2026-06-27T01:00:00Z"));
        entityManager.persist(reportReview);
        entityManager.flush();
        entityManager.clear();

        var result = reportReviewRepository.findById(reportArchive.getId());

        assertTrue(result.isPresent());
        assertEquals(reportArchive.getId(), result.get().getReportId());
        assertEquals("Feedback do professor", result.get().getComment());
        assertEquals("s3:prod/reviews/15/correction.pdf", result.get().getCorrectionUrl());
        assertNotNull(result.get().getReport());
        assertEquals(reportArchive.getId(), result.get().getReport().getId());
    }
}
