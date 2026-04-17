package com.fluxo.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    String username,
    String email,
    @NotBlank(message = "A senha é obrigatória") String password
) {}