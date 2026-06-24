package com.fluxo.project.controller;

import com.fluxo.project.dto.ProjectListResponseDto;
import com.fluxo.project.service.ProjectService;
import com.fluxo.project.dto.ProjectDetailsResponseDto;
import org.springframework.web.bind.annotation.PathVariable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fluxo.project.dto.ProjectUpdateResponseDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "8. Projetos", description = "Endpoints para listagem de projetos do estudante")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(
            summary = "Listar projetos do aluno autenticado",
            description = "Retorna todos os projetos em que o aluno autenticado já participou ou participa, " +
                    "incluindo projetos históricos, ordenados do período mais recente para o mais antigo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projetos do aluno retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado")
    })
    public ResponseEntity<List<ProjectListResponseDto>> getMyProjects() {
        return ResponseEntity.ok(projectService.getMyProjects());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar detalhes do projeto",
            description = "Retorna os detalhes completos de um projeto vinculado ao aluno autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhes do projeto retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado"),
            @ApiResponse(responseCode = "404", description = "Projeto nao encontrado")
    })
    public ResponseEntity<ProjectDetailsResponseDto> getProjectDetails(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(projectService.getProjectDetails(id));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Editar parcialmente um projeto",
            description = "Permite editar summary, description, thumbnail e foto do grupo de um projeto em andamento. Restrito a AGES IV do projeto."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Formato de arquivo inválido"),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuario nao possui permissao para editar este projeto"),
            @ApiResponse(responseCode = "404", description = "Projeto nao encontrado"),
            @ApiResponse(responseCode = "422", description = "Projeto nao esta em andamento")
    })
    public ResponseEntity<ProjectUpdateResponseDto> updateProject(
            @PathVariable Integer id,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile thumbnail,
            @RequestParam(required = false) MultipartFile groupPhoto
    ) {
        return ResponseEntity.ok(projectService.updateProject(
                id,
                summary,
                description,
                thumbnail,
                groupPhoto
        ));
    }
}
