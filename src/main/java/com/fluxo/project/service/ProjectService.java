package com.fluxo.project.service;

import com.fluxo.project.dto.ProjectListResponseDto;
import com.fluxo.project.entity.Project;
import com.fluxo.user.entity.StudentHistory;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.repository.StudentHistoryRepository;
import com.fluxo.user.repository.StudentProfileRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final StudentProfileRepository studentProfileRepository;
    private final StudentHistoryRepository studentHistoryRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public List<ProjectListResponseDto> getMyProjects() {
        Integer userId = authenticatedUserService.getUserId();

        List<StudentHistory> histories = studentHistoryRepository.findByStudentUserIdOrderByRecent(userId);

        Map<String, Integer> memberCountByProjectSemester = new HashMap<>();
        for (StudentHistory history : histories) {
            if (history.getProject() != null && history.getProject().getSemesterYear() != null) {
                String key = history.getProject().getId() + "_" + history.getProject().getSemesterYear();
                Set<Integer> studentsInProjectSemester = histories.stream()
                        .filter(h -> h.getProject() != null 
                            && h.getProject().getId().equals(history.getProject().getId())
                            && h.getProject().getSemesterYear().equals(history.getProject().getSemesterYear()))
                        .map(h -> h.getStudentUser().getId())
                        .collect(Collectors.toSet());
                memberCountByProjectSemester.put(key, studentsInProjectSemester.size());
            }
        }

        Map<Integer, ProjectListResponseDto> dtoMap = new LinkedHashMap<>();

        for (StudentHistory history : histories) {
            Project project = history.getProject();
            if (project != null && !dtoMap.containsKey(project.getId())) {
                List<String> technologies = project.getTechnologies() != null
                        ? project.getTechnologies().stream()
                            .map(tech -> tech.getName())
                            .collect(Collectors.toList())
                        : new ArrayList<>();

                String memberCountKey = project.getId() + "_" + project.getSemesterYear();
                Integer membersCount = memberCountByProjectSemester.getOrDefault(memberCountKey, 0);

                ProjectListResponseDto dto = new ProjectListResponseDto(
                        project.getId(),
                        project.getName(),
                        project.getSummary(),
                        project.getStatus(),
                        history.getStudentStatus().toString(),
                        project.getPeriod(),
                        project.getSemesterYear(),
                        history.getAgesLevel(),
                        project.getGitLabLink(),
                        membersCount,
                        technologies,
                        project.getThumbnailUrl(),
                        project.getGroupPhotoUrl()
                );
                dtoMap.put(project.getId(), dto);
            }
        }

        return new ArrayList<>(dtoMap.values());
    }
}
