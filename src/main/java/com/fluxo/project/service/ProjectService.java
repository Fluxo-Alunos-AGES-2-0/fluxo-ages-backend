package com.fluxo.project.service;

import com.fluxo.project.dto.ProjectListResponseDto;
import com.fluxo.project.entity.Project;
import com.fluxo.user.entity.StudentHistory;
import com.fluxo.user.repository.StudentHistoryRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.fluxo.project.dto.ProjectDetailsResponseDto;
import com.fluxo.project.dto.ProjectTeacherResponseDto;
import com.fluxo.project.dto.ProjectTeamMemberResponseDto;
import com.fluxo.project.dto.TechnologyDto;
import com.fluxo.project.exception.ProjectNotFoundException;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.StudentProfileRepository;
import org.springframework.transaction.annotation.Transactional;

import com.fluxo.infra.exception.StorageException;
import com.fluxo.infra.storage.S3StorageService;
import com.fluxo.project.dto.ProjectUpdateResponseDto;
import com.fluxo.project.entity.ProjectStatus;
import com.fluxo.project.exception.InvalidProjectImageException;
import com.fluxo.project.exception.ProjectAccessDeniedException;
import com.fluxo.project.exception.ProjectNotEditableException;
import com.fluxo.project.exception.ProjectStorageException;
import com.fluxo.project.repository.ProjectRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final int AGES_IV = 4;
    private static final String S3_REFERENCE_PREFIX = "s3:";
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );
    private final StudentHistoryRepository studentHistoryRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final ProjectRepository projectRepository;
    private final S3StorageService s3StorageService;

    public List<ProjectListResponseDto> getMyProjects() {
        Integer userId = authenticatedUserService.getUserId();

        List<StudentHistory> userHistories = studentHistoryRepository.findByStudentUserIdOrderByRecent(userId);

        // Deduplica projetos e mantém ordem dos mais recentes
        Map<Integer, StudentHistory> uniqueProjectsMap = new LinkedHashMap<>();
        for (StudentHistory history : userHistories) {
            if (history.getProject() != null) {
                uniqueProjectsMap.putIfAbsent(history.getProject().getId(), history);
            }
        }

        return uniqueProjectsMap.values().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectDetailsResponseDto getProjectDetails(Integer projectId) {
        Integer userId = authenticatedUserService.getUserId();

        Optional<StudentHistory> studentHistoryOpt =
                studentHistoryRepository.findFirstByStudentUserIdAndProjectIdOrderBySemesterYearDesc(userId, projectId);

        Optional<StudentProfile> studentProfileOpt =
                studentProfileRepository.findByStudentUserIdAndTeamProjectId(userId, projectId);

        if (studentHistoryOpt.isEmpty() && studentProfileOpt.isEmpty()) {
            throw new ProjectNotFoundException("Projeto não encontrado.");
        }

        Project project = studentHistoryOpt
                .map(StudentHistory::getProject)
                .orElseGet(() -> studentProfileOpt.get().getTeam().getProject());

        Integer agesLevel = studentHistoryOpt
                .map(StudentHistory::getAgesLevel)
                .orElseGet(() -> studentProfileOpt.get().getAgesPosition());

        List<ProjectTeamMemberResponseDto> team = buildProjectTeam(project);
        
        List<TechnologyDto> technologies = extractTechnologies(project);
        User teacher = project.getTeacherUser();

        return new ProjectDetailsResponseDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus().toString(),
                project.getPeriod(),
                project.getSemesterYear(),
                agesLevel,
                team.size(),
                project.getGitLabLink(),
                new ProjectTeacherResponseDto(
                        teacher.getId(),
                        teacher.getName()
                ),
                team,
                technologies,
                resolveProjectImageUrl(project.getThumbnailUrl()),
                resolveProjectImageUrl(project.getGroupPhotoUrl())
        );
    }

    @Transactional
    public ProjectUpdateResponseDto updateProject(
            Integer projectId,
            String summary,
            String description,
            MultipartFile thumbnail,
            MultipartFile groupPhoto
    ) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado."));

        validateUserCanEditProject(projectId);

        if (project.getStatus() != ProjectStatus.EM_ANDAMENTO) {
            throw new ProjectNotEditableException("Projeto não pode ser editado porque não está em andamento.");
        }

        if (summary != null) {
            project.setSummary(summary);
        }

        if (description != null) {
            project.setDescription(description);
        }

        if (thumbnail != null && !thumbnail.isEmpty()) {
            project.setThumbnailUrl(storeProjectImage(project.getId(), thumbnail, "thumbnail"));
        }

        if (groupPhoto != null && !groupPhoto.isEmpty()) {
            project.setGroupPhotoUrl(storeProjectImage(project.getId(), groupPhoto, "group"));
        }

        Project savedProject = projectRepository.save(project);

        return new ProjectUpdateResponseDto(
                savedProject.getId(),
                savedProject.getSummary(),
                savedProject.getDescription(),
                resolveProjectImageUrl(savedProject.getThumbnailUrl()),
                resolveProjectImageUrl(savedProject.getGroupPhotoUrl())
        );
    }

    private ProjectListResponseDto convertToDto(StudentHistory studentHistory) {
        Project project = studentHistory.getProject();
        
        List<ProjectTeamMemberResponseDto> team = buildProjectTeam(project);
        List<TechnologyDto> technologies = extractTechnologies(project);

        return new ProjectListResponseDto(
                project.getId(),
                project.getName(),
                project.getSummary(),
                project.getStatus().toString(),
                studentHistory.getStudentStatus().toString(),
                project.getPeriod(),
                project.getSemesterYear(),
                studentHistory.getAgesLevel(),
                project.getGitLabLink(),
                team.size(),
                team,
                technologies,
                resolveProjectImageUrl(project.getThumbnailUrl()),
                resolveProjectImageUrl(project.getGroupPhotoUrl())
        );
    }

    private List<TechnologyDto> extractTechnologies(Project project) {
        if (project.getTechnologies() == null || project.getTechnologies().isEmpty()) {
            return new ArrayList<>();
        }
        
        return project.getTechnologies().stream()
                .map(tech -> new TechnologyDto(
                        tech.getId(),
                        tech.getName(),
                        tech.getIconUrl() // <-- Agora o ícone vai junto!
                ))
                .toList();
    }

    private List<ProjectTeamMemberResponseDto> buildProjectTeam(Project project) {
        Map<Integer, User> usersById = new LinkedHashMap<>();
        Map<Integer, Integer> agesLevelById = new HashMap<>();

        studentHistoryRepository.findByProject(project).forEach(history -> {
            User user = history.getStudentUser();
            usersById.putIfAbsent(user.getId(), user);
            agesLevelById.putIfAbsent(user.getId(), history.getAgesLevel());
        });

        studentProfileRepository.findByTeamProjectId(project.getId()).forEach(profile -> {
            User user = profile.getStudentUser();
            usersById.putIfAbsent(user.getId(), user);
            agesLevelById.put(user.getId(), profile.getAgesPosition());
        });

        if (usersById.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, StudentProfile> profilesByUserId = studentProfileRepository
                .findByStudentUserIdIn(usersById.keySet())
                .stream()
                .collect(Collectors.toMap(profile -> profile.getStudentUser().getId(), profile -> profile, (first, second) -> first));

        return usersById.values()
                .stream()
                .map(user -> {
                    StudentProfile profile = profilesByUserId.get(user.getId());
                    String avatarUrl = (profile != null) ? profile.getImageUrl() : null;
                    Integer agesLevel = agesLevelById.get(user.getId());

                    return new ProjectTeamMemberResponseDto(
                            user.getId(),
                            user.getName(),
                            avatarUrl,
                            agesLevel
                    );
                })
                .collect(Collectors.toList());
    }
    private void validateUserCanEditProject(Integer projectId) {
        Integer userId = authenticatedUserService.getUserId();

        StudentProfile studentProfile = studentProfileRepository
                .findByStudentUserIdAndTeamProjectId(userId, projectId)
                .orElseThrow(() -> new ProjectAccessDeniedException(
                        "Usuário autenticado não possui permissão para editar este projeto."
                ));

        if (!Integer.valueOf(AGES_IV).equals(studentProfile.getAgesPosition())) {
            throw new ProjectAccessDeniedException(
                    "Usuário autenticado não possui permissão para editar este projeto."
            );
        }
    }

    private String storeProjectImage(Integer projectId, MultipartFile image, String imageType) {
        validateProjectImage(image);

        String contentType = image.getContentType();
        String extension = resolveImageExtension(contentType);
        String relativePath = "projects/" + projectId + "/" + UUID.randomUUID() + "_" + imageType + extension;
        String key = s3StorageService.buildKey(relativePath);

        try {
            s3StorageService.uploadObject(key, contentType, image.getBytes());
            return S3_REFERENCE_PREFIX + key;
        } catch (IOException | StorageException e) {
            throw new ProjectStorageException("Não foi possível salvar imagem do projeto.", e);
        }
    }

    private void validateProjectImage(MultipartFile image) {
        String contentType = image.getContentType();

        if (!StringUtils.hasText(contentType)
                || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new InvalidProjectImageException("Formato de arquivo inválido.");
        }
    }

    private String resolveImageExtension(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new InvalidProjectImageException("Formato de arquivo inválido.");
        };
    }

    private String resolveProjectImageUrl(String fileReference) {
        if (!StringUtils.hasText(fileReference)) {
            return fileReference;
        }

        if (fileReference.startsWith(S3_REFERENCE_PREFIX)) {
            String key = fileReference.substring(S3_REFERENCE_PREFIX.length());
            return createProjectImageAccessUrl(key);
        }

        if (fileReference.startsWith("/") || looksLikeAbsoluteUrl(fileReference)) {
            return fileReference;
        }

        return createProjectImageAccessUrl(fileReference);
    }

    private String createProjectImageAccessUrl(String key) {
        try {
            return s3StorageService.createGetPresignedUrl(key);
        } catch (StorageException e) {
            throw new ProjectStorageException("Não foi possível gerar URL de acesso da imagem do projeto.", e);
        }
    }

    private boolean looksLikeAbsoluteUrl(String value) {
        try {
            return URI.create(value).isAbsolute();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
