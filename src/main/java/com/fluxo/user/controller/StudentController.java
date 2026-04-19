package com.fluxo.user.controller;

import com.fluxo.user.dto.StudentProfileResponseDto;
import com.fluxo.user.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Tag(name = "2. Estudantes", description = "Endpoints para gerenciamento de perfis de estudantes")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/me")
    @Operation(summary = "Obter perfil logado", description = "Retorna os dados do estudante atualmente autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil do estudante retornado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Estudante não encontrado")
    })
    public ResponseEntity<StudentProfileResponseDto> getLoggedStudentProfile() {
        return studentService.getLoggedStudentProfile()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
