package com.fluxo.project.service;

import com.fluxo.project.dto.ProjectListResponseDto;
import com.fluxo.project.entity.Project;
import com.fluxo.user.entity.StudentHistory;
import com.fluxo.user.repository.StudentHistoryRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final StudentHistoryRepository studentHistoryRepository;
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

    private ProjectListResponseDto convertToDto(StudentHistory studentHistory) {
        Project project = studentHistory.getProject();
        
        Integer membersCount = countProjectMembers(project);
        List<String> technologies = extractTechnologies(project);

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
                membersCount,
                technologies,
                project.getThumbnailUrl(),
                project.getGroupPhotoUrl()
        );
    }

    private Integer countProjectMembers(Project project) {
        Set<Integer> memberIds = studentHistoryRepository
                .findByProject(project)
                .stream()
                .map(history -> history.getStudentUser().getId())
                .collect(Collectors.toSet());
        
        return memberIds.size();
    }

    private List<String> extractTechnologies(Project project) {
        if (project.getTechnologies() == null || project.getTechnologies().isEmpty()) {
            return new ArrayList<>();
        }
        
        return project.getTechnologies().stream()
                .map(tech -> tech.getName())
                .collect(Collectors.toList());
    }
}
