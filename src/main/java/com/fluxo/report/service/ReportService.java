package com.fluxo.report.service;

import com.fluxo.project.entity.Project;
import com.fluxo.project.repository.ProjectRepository;
import com.fluxo.report.entity.ReportArchive;
import com.fluxo.report.repository.ReportArchiveRepository;
import com.fluxo.report.dto.ReportArchiveResponseDto;
import com.fluxo.report.exception.InvalidReportFileException;
import com.fluxo.report.exception.StudentProjectNotFoundException;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ReportService {

    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final ReportArchiveRepository reportArchiveRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final FileStorageService fileStorageService;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    public ReportService(ReportArchiveRepository reportArchiveRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            FileStorageService fileStorageService) {
        this.reportArchiveRepository = reportArchiveRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Valida o arquivo enviado verificando formato e tamanho.
     *
     * @param file O arquivo a validar
     * @throws InvalidReportFileException Se o arquivo for inválido
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidReportFileException("Arquivo não pode estar vazio.");
        }

        if (file.getContentType() == null || !file.getContentType().equals(PDF_CONTENT_TYPE)) {
            throw new InvalidReportFileException("Formato inválido. Apenas arquivos PDF são aceitos.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidReportFileException("O arquivo não pode exceder 25MB.");
        }
    }

    /**
     * Busca o projeto atual do aluno através do seu StudentProfile e Team.
     *
     * @param userId O ID do usuário (aluno)
     * @return O projeto atual do aluno
     * @throws StudentProjectNotFoundException Se o projeto não for encontrado
     */
    private Project findCurrentProject(Integer userId) {
        java.util.List<com.fluxo.user.entity.StudentProfile> result = entityManager.createQuery(
                "SELECT sp FROM StudentProfile sp WHERE sp.studentUser.id = :userId",
                com.fluxo.user.entity.StudentProfile.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();

        if (result.isEmpty() || result.get(0).getTeam() == null || result.get(0).getTeam().getProject() == null) {
            throw new StudentProjectNotFoundException("Projeto atual do aluno não encontrado.");
        }

        return result.get(0).getTeam().getProject();
    }

    /**
     * Interface funcional para encapsular a lógica de busca de relatório existente,
     * permitindo diferentes estratégias de busca para cada tipo de relatório.
     */
    @FunctionalInterface
    private interface ExistingReportFinder {
        Optional<ReportArchive> find(Integer studentId, Project project);
    }

    /**
     * Fluxo comum de upload de relatório. Reduz duplicação entre uploadProgressReport
     * e uploadFinalReport ao encapsular a lógica compartilhada.
     *
     * @param file O arquivo a ser enviado
     * @param studentId O ID do aluno
     * @param reportType O tipo de relatório (1=Andamento, 2=Final)
     * @param existingReportFinder Função que busca relatório existente do tipo específico
     * @return DTO com informações do relatório salvo
     */
    private ReportArchiveResponseDto uploadReport(
            MultipartFile file,
            Integer studentId,
            int reportType,
            ExistingReportFinder existingReportFinder) {

        // Validar arquivo
        validateFile(file);

        // Buscar referências necessárias
        User studentRef = userRepository.getReferenceById(studentId);
        Project projectRef = findCurrentProject(studentId);

        // Buscar relatório existente
        Optional<ReportArchive> existingReportOpt = existingReportFinder.find(studentId, projectRef);

        ReportArchive report;

        if (existingReportOpt.isPresent()) {
            // Atualizar relatório existente
            report = existingReportOpt.get();

            // Remover arquivo antigo do storage
            fileStorageService.deleteFile(report.getUrlArchive());

            report.setEditDate(LocalDate.now());
        } else {
            // Criar novo relatório
            report = new ReportArchive();

            report.setStudentUser(studentRef);
            report.setProject(projectRef);
            report.setType(reportType);
            report.setCreateDate(LocalDate.now());
            report.setEditDate(LocalDate.now());
        }

        // Salvar novo arquivo no storage
        String fileUrl = fileStorageService.saveFile(file);
        report.setUrlArchive(fileUrl);

        // Persistir no banco
        ReportArchive savedReport = reportArchiveRepository.save(report);

        return buildResponse(savedReport);
    }

    /**
     * Constrói o DTO de resposta a partir da entidade ReportArchive.
     */
    private ReportArchiveResponseDto buildResponse(ReportArchive report) {
        return new ReportArchiveResponseDto(
                report.getId(),
                report.getUrlArchive(),
                report.getCreateDate().atStartOfDay()
                        .atOffset(java.time.ZoneOffset.UTC)
                        .toInstant()
                        .toString()
        );
    }

    /**
     * Faz upload de relatório de andamento do aluno.
     * Se já existir um relatório anterior, ele será sobrescrito.
     *
     * @param file O arquivo PDF do relatório
     * @param studentId O ID do aluno
     * @return DTO com informações do relatório salvo
     */
    @Transactional
    public ReportArchiveResponseDto uploadProgressReport(MultipartFile file, Integer studentId) {
        return uploadReport(
                file,
                studentId,
                1,
                (id, project) -> reportArchiveRepository.findByStudentUserIdAndType(id, 1)
        );
    }

    /**
     * Faz upload de relatório final do aluno.
     * Se já existir um relatório anterior, ele será sobrescrito.
     *
     * @param file O arquivo PDF do relatório
     * @param studentId O ID do aluno
     * @return DTO com informações do relatório salvo
     */
    @Transactional
    public ReportArchiveResponseDto uploadFinalReport(MultipartFile file, Integer studentId) {
        Project projectRef = findCurrentProject(studentId);
        return uploadReport(
                file,
                studentId,
                2,
                (id, project) -> reportArchiveRepository.findByStudentUserIdAndProjectIdAndType(id, project.getId(), 2)
        );
    }
}
