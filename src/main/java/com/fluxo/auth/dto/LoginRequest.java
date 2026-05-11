package com.fluxo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "O login é obrigatório") @Schema(description = "Pode ser enviado o Nome do usuário ou o E-mail cadastrado", example = "Aluno") String username,

        @NotBlank(message = "A senha é obrigatória") @Schema(description = "Senha do usuário", example = "senha123") String password) {
}