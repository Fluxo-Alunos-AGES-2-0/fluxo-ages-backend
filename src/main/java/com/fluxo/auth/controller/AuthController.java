package com.fluxo.auth.controller;

import com.fluxo.auth.dto.LoginRequest;
import com.fluxo.auth.dto.LoginResponse;
import com.fluxo.auth.dto.UserResponse;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.UserRepository;
import com.fluxo.auth.service.JwtService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Injetamos o UserRepository aqui
    public AuthController(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        
        // 1. Busca o usuário no Banco de Dados Real
        Optional<User> userOptional = userRepository.findByUsername(request.username());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciais inválidas"));
        }

        User user = userOptional.get();

        // 2. Compara a senha do Front-end com a senha criptografada que está no Banco
        boolean isPasswordValid = passwordEncoder.matches(request.password(), user.getPassword());
        
        if (!isPasswordValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciais inválidas"));
        }

        // 3. Monta os dados para o Payload
        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
        
        // 4. Gera o Token
        String token = jwtService.generateToken(user.getId(), user.getName(), user.getRole());

        // 5. Retorna 200 OK
        LoginResponse response = new LoginResponse(token, jwtService.getExpirationTime(), userResponse);
        return ResponseEntity.ok(response);
    }
}