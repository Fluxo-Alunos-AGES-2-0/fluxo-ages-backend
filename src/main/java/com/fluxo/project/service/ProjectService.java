package com.fluxo.project.service;

import com.fluxo.project.dto.ProjectListResponseDto;
import com.fluxo.project.entity.Project;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.repository.StudentHistoryRepository;
import com.fluxo.user.repository.StudentProfileRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final StudentProfileRepository studentProfileRepository;
    private final StudentHistoryRepository studentHistoryRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public List<ProjectListResponseDto> getMyProjects() {
        Integer userId = authenticatedUserService.getUserId();

        List<Project> projects = new ArrayList<>();

        studentProfileRepository.findByStudentUserId(userId)
                .map(StudentProfile::getTeam)
                .map(team -> team == null ? null : team.getProject())
                .ifPresent(projects::add);

        studentHistoryRepository.findByStudentUserId(userId)
                .stream()
                .map(history -> history.getProject())
                .filter(p -> p != null)
                .forEach(projects::add);

        Set<Integer> seen = new HashSet<>();
        List<ProjectListResponseDto> result = new ArrayList<>();
        for (Project project : projects) {
            if (seen.add(project.getId())) {
                result.add(new ProjectListResponseDto(project.getId(), project.getName()));
            }
        }
        return result;
    }
}
