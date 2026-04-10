package com.fluxo.auth.controller;

import com.fluxo.auth.dto.LoginRequest;
import com.fluxo.auth.dto.LoginResponse;
import com.fluxo.auth.dto.UserResponse;
import com.fluxo.auth.service.JwtService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    // A senha "senha123" gerada via BCrypt para simular o banco
    private static final String MOCK_PASSWORD_HASH = "$2a$10$wT0X8U8hFvJ8F9.7O.o4/Ou7oR2U.gH/3xUoYx4fM8qM5WlZ5/jK2";

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        
        // 1. Simula a verificação do usuário no banco
        if (!"joaosilva".equals(request.username())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciais inválidas"));
        }

        // 2. Compara a senha enviada no body com o Hash salvo no banco
        boolean isPasswordValid = passwordEncoder.matches(request.password(), MOCK_PASSWORD_HASH);
        
        if (!isPasswordValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciais inválidas"));
        }

        // 3. Monta o usuário mockado
        UserResponse user = new UserResponse(1L, "João Silva", "joao.silva@edu.br", "STUDENT");
        
        // 4. Gera o Token JWT
        String token = jwtService.generateToken(user.id(), user.name(), user.role());

        // 5. Retorna o sucesso (200 OK)
        LoginResponse response = new LoginResponse(token, jwtService.getExpirationTime(), user);
        
        return ResponseEntity.ok(response);
    }
    @GetMapping("/ping")
        public String ping() {
        return "O Controller está vivo!";
}
}