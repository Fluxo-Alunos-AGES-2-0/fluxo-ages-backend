package com.fluxo.report.dto;

public record ReportUploadUrlResponseDto(
        String uploadUrl,
        String fileReference,
        String method,
        String contentType
) {
}
