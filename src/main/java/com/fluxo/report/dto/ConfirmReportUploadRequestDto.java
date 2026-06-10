package com.fluxo.report.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmReportUploadRequestDto(
        @NotBlank(message = "A referência do arquivo é obrigatória")
        String fileReference
) {
}
