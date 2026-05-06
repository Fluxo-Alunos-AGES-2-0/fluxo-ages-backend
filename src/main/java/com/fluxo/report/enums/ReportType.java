package com.fluxo.report.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipos de relatório disponíveis")
public enum ReportType {
    @Schema(description = "Relatório de Andamento")
    RA,
    @Schema(description = "Relatório Final")
    RF,
    @Schema(description = "Relatório de Sprint")
    SPRINT,
    @Schema(description = "Registro de Horas")
    HOURS
}
