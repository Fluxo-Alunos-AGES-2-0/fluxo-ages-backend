package com.fluxo.project.dto;

import java.util.List;

public record ProjectListResponseDto(
        Integer id,
        String name,
        String summary,
        String projectStatus,
        String studentStatus,
        String period,
        String semesterYear,
        Integer agesLevel,
        String gitLabLink,
        Integer membersCount,
        List<String> technologies,
        String thumbnailUrl,
        String groupPhotoUrl
) {}