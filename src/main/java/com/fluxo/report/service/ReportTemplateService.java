package com.fluxo.report.service;

import com.fluxo.infra.exception.StorageException;
import com.fluxo.infra.storage.S3StorageService;
import com.fluxo.report.exception.ReportTemplateNotFoundException;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.repository.StudentProfileRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportTemplateService {

    private static final String TEMPLATE_FOLDER = "templates";

    private final AuthenticatedUserService authenticatedUserService;
    private final StudentProfileRepository studentProfileRepository;
    private final S3StorageService s3StorageService;

    public record ReportTemplateFile(byte[] content, String filename) {
    }

    public ReportTemplateFile loadReportTemplate() {
        Integer userId = authenticatedUserService.getUserId();
        StudentProfile studentProfile = studentProfileRepository.findByStudentUserId(userId)
                .orElseThrow(() -> new ReportTemplateNotFoundException(
                        "Perfil do aluno nao encontrado para determinar o template de relatorio."
                ));

        String filename = resolveTemplateFilename(studentProfile.getAgesPosition());
        String key = s3StorageService.buildKey(TEMPLATE_FOLDER + "/" + filename);

        try {
            s3StorageService.assertObjectExists(key);
            return new ReportTemplateFile(s3StorageService.downloadObject(key), filename);
        } catch (StorageException e) {
            throw new ReportTemplateNotFoundException("Template de relatorio nao encontrado no S3.", e);
        }
    }

    private String resolveTemplateFilename(Integer agesPosition) {
        return switch (agesPosition) {
            case 1 -> "ages-I-template.docx";
            case 2 -> "ages-II-template.docx";
            case 3 -> "ages-III-template.docx";
            case 4 -> "ages-IV-template.docx";
            default -> throw new ReportTemplateNotFoundException(
                    "Nivel AGES invalido para template de relatorio."
            );
        };
    }
}
