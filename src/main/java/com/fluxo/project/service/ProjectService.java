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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final StudentHistoryRepository studentHistoryRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AuthenticatedUserService authenticatedUserService;

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
                project.getThumbnailUrl(),
                project.getGroupPhotoUrl()
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
                project.getThumbnailUrl(),
                project.getGroupPhotoUrl()
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

        studentHistoryRepository.findByProject(project)
                .stream()
                .map(StudentHistory::getStudentUser)
                .forEach(user -> usersById.putIfAbsent(user.getId(), user));

        studentProfileRepository.findByTeamProjectId(project.getId())
                .stream()
                .map(StudentProfile::getStudentUser)
                .forEach(user -> usersById.putIfAbsent(user.getId(), user));

        if (usersById.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, StudentProfile> profilesByUserId = studentProfileRepository
                .findByStudentUserIdIn(usersById.keySet())
                .stream()
                .collect(Collectors.toMap(
                        profile -> profile.getStudentUser().getId(),
                        profile -> profile,
                        (first, second) -> first
                ));

        return usersById.values()
                .stream()
                .map(user -> {
                    StudentProfile profile = profilesByUserId.get(user.getId());

                    String avatarUrl = profile == null ? null : profile.getImageUrl();

                    return new ProjectTeamMemberResponseDto(
                            user.getId(),
                            user.getName(),
                            avatarUrl
                    );
                })
                .collect(Collectors.toList());
    }
}
