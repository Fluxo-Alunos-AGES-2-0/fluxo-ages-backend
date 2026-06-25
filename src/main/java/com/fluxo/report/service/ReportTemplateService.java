package com.fluxo.report.service;

import com.fluxo.report.exception.StudentProjectNotFoundException;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.StudentProfileRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportTemplateService {

    private final AuthenticatedUserService authenticatedUserService;
    private final StudentProfileRepository studentProfileRepository;
    private final FileStorageService fileStorageService;

    public String getTemplateDownloadUrl() {
        User user = authenticatedUserService.getAuthenticatedUser();
        StudentProfile profile = studentProfileRepository.findByStudentUserId(user.getId())
                .orElseThrow(() -> new StudentProjectNotFoundException("Perfil de estudante do usuário não encontrado."));

        Integer agesLevel = profile.getAgesPosition();
        if (agesLevel == null) {
            throw new StudentProjectNotFoundException("Nível AGES do estudante não definido.");
        }

        return fileStorageService.createTemplateGetPresignedUrl(agesLevel);
    }
}
