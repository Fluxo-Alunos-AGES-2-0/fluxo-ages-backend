package com.fluxo.auth.controller;

import com.fluxo.auth.dto.ForgotPasswordRequest;
import com.fluxo.auth.dto.LoginRequest;
import com.fluxo.auth.dto.LoginResponse;
import com.fluxo.auth.dto.ResetPasswordRequest;
import com.fluxo.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/auth")
@Tag(name = "1. Autenticação", description = "Endpoints para gerenciamento de login e recuperação de senha")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Autentica o usuário e retorna o token de acesso (JWT)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperação de senha",
               description = "Envia um email com um link para redefinir a senha")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email de recuperação enviado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Email é obrigatório")
    })
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        if (forgotPasswordRequest.getEmail() == null || forgotPasswordRequest.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Email é obrigatório"));
        }

        authService.forgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok()
                .body(Map.of("message", "Se o email existir, as instrucoes de recuperacao serao enviadas"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha",
               description = "Redefine a senha do usuário a partir de um token de recuperação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Link de recuperação inválido ou expirado ou senha fraca")
    })
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        if (resetPasswordRequest.getNewPassword() == null || resetPasswordRequest.getNewPassword().length() < 8) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Senha deve ter no mínimo 8 caracteres"));
        }

        try {
            authService.resetPassword(resetPasswordRequest);
            return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
