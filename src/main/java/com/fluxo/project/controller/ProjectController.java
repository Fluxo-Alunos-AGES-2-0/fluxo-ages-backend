package com.fluxo.project.controller;

import com.fluxo.project.dto.ProjectListResponseDto;
import com.fluxo.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@Tag(name = "8. Projetos", description = "Endpoints para listagem de projetos do estudante")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/me")
    @Operation(
            summary = "Listar projetos do aluno autenticado",
            description = "Retorna o projeto atual e os projetos historicos do aluno autenticado, sem duplicatas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projetos do aluno retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado")
    })
    public ResponseEntity<List<ProjectListResponseDto>> getMyProjects() {
        return ResponseEntity.ok(projectService.getMyProjects());
    }
}
