package com.fluxo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "O email e obrigatorio")
        @Email(message = "O email deve ser valido")
        String email
) {
}
