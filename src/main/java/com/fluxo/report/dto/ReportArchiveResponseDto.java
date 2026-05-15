package com.fluxo.report.dto;

public record ReportArchiveResponseDto(
    Integer id,
    String archiveUrl,
    String createdAt
) {}