package com.fluxo.project.dto;

public record ProjectUpdateResponseDto(
        Integer id,
        String summary,
        String description,
        String thumbnailUrl,
        String groupPhotoUrl
) {
}