package com.fluxo.project.dto;

import java.util.List;

public record ProjectDetailsResponseDto(
        Integer id,
        String name,
        String description,
        String projectStatus,
        String period,
        String semesterYear,
        Integer agesLevel,
        Integer membersCount,
        String gitLabLink,
        ProjectTeacherResponseDto teacher,
        List<ProjectTeamMemberResponseDto> team,
        List<TechnologyDto> technologies,        String thumbnailUrl,
        String groupPhotoUrl
) {
}