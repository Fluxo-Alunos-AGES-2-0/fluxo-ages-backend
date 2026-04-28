package com.fluxo.report.service;

import com.fluxo.project.entity.Project;
import com.fluxo.project.repository.ProjectRepository;
import com.fluxo.report.entity.ReportArchive;
import com.fluxo.report.repository.ReportArchiveRepository;
import com.fluxo.report.dto.ReportArchiveResponseDto;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportArchiveRepository reportArchiveRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private final Path uploadDir = Paths.get("uploads/reports/").toAbsolutePath();

    @Value("${app.storage.base-url:https://storage.example.com/reports/}")
    private String baseUrl;

    public ReportService(ReportArchiveRepository reportArchiveRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository) {
        this.reportArchiveRepository = reportArchiveRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        initDirectory();
    }

    private void initDirectory() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads.", e);
        }
    }

    @Transactional
    public ReportArchiveResponseDto uploadProgressReport(MultipartFile file, Integer studentId) {

        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato inválido. Apenas arquivos PDF são aceitos.");
        }

        if (file.getSize() > 25 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo não pode exceder 25MB.");
        }

        try {
            Optional<ReportArchive> existingReportOpt = reportArchiveRepository.findByStudentUserIdAndType(studentId,
                    1);
            ReportArchive report;

            String fileName = UUID.randomUUID() + ".pdf";
            Path filePath = uploadDir.resolve(fileName);

            User studentRef = userRepository.getReferenceById(studentId);
            Project projectRef = findCurrentProject(studentId);

            if (existingReportOpt.isPresent()) {
                report = existingReportOpt.get();

                String oldFileName = report.getUrlArchive().substring(report.getUrlArchive().lastIndexOf("/") + 1);
                Path oldFilePath = uploadDir.resolve(oldFileName);
                Files.deleteIfExists(oldFilePath);

                report.setProject(projectRef);
                report.setEditDate(LocalDate.now());

                report.setUrlArchive(baseUrl + fileName);
            } else {
                report = new ReportArchive();

                report.setStudentUser(studentRef);
                report.setProject(projectRef);
                report.setType(1);
                report.setCreateDate(LocalDate.now());
                report.setEditDate(LocalDate.now());

                report.setUrlArchive(baseUrl + fileName);
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            ReportArchive savedReport = reportArchiveRepository.save(report);

            return new ReportArchiveResponseDto(
                    savedReport.getId(),
                    savedReport.getUrlArchive(),
                    savedReport.getCreateDate().atStartOfDay().atOffset(java.time.ZoneOffset.UTC).toInstant()
                            .toString());

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao processar o upload do arquivo.");
        }
    }

    @Transactional
    public ReportArchiveResponseDto uploadFinalReport(MultipartFile file, Integer studentId) {

        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato inválido. Apenas arquivos PDF são aceitos.");
        }

        if (file.getSize() > 25 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo não pode exceder 25MB.");
        }

        try {
            User studentRef = userRepository.getReferenceById(studentId);
            Project projectRef = findCurrentProject(studentId);

            Optional<ReportArchive> existingReportOpt = reportArchiveRepository
                    .findByStudentUserIdAndProjectIdAndType(studentId, projectRef.getId(), 2);
            ReportArchive report;

            String fileName = UUID.randomUUID() + ".pdf";
            Path filePath = uploadDir.resolve(fileName);

            if (existingReportOpt.isPresent()) {
                report = existingReportOpt.get();

                String oldFileName = report.getUrlArchive().substring(report.getUrlArchive().lastIndexOf("/") + 1);
                Path oldFilePath = uploadDir.resolve(oldFileName);
                Files.deleteIfExists(oldFilePath);

                report.setEditDate(LocalDate.now());
                report.setUrlArchive(baseUrl + fileName);
            } else {
                report = new ReportArchive();

                report.setStudentUser(studentRef);
                report.setProject(projectRef);
                report.setType(2);
                report.setCreateDate(LocalDate.now());
                report.setEditDate(LocalDate.now());
                report.setUrlArchive(baseUrl + fileName);
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            ReportArchive savedReport = reportArchiveRepository.save(report);

            return new ReportArchiveResponseDto(
                    savedReport.getId(),
                    savedReport.getUrlArchive(),
                    savedReport.getCreateDate().atStartOfDay().atOffset(java.time.ZoneOffset.UTC).toInstant()
                            .toString());

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao processar o upload do arquivo.");
        }
    }

    private Project findCurrentProject(Integer userId) {
        java.util.List<com.fluxo.user.entity.StudentProfile> result = entityManager.createQuery(
                "SELECT sp FROM StudentProfile sp WHERE sp.studentUser.id = :userId",
                com.fluxo.user.entity.StudentProfile.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();

        if (result.isEmpty() || result.get(0).getTeam() == null || result.get(0).getTeam().getProject() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Projeto atual do aluno não encontrado.");
        }

        return result.get(0).getTeam().getProject();
    }
}