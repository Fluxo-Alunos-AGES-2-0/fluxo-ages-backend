package com.fluxo.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "O username é obrigatório") String username,
    @NotBlank(message = "A senha é obrigatória") String password
) {}