package com.fluxo.auth.controller;

import com.fluxo.auth.dto.ForgotPasswordRequest;
import com.fluxo.auth.dto.LoginRequest;
import com.fluxo.auth.dto.LoginResponse;
import com.fluxo.auth.dto.ResetPasswordRequest;
import com.fluxo.auth.dto.UserResponse;
import com.fluxo.auth.service.JwtService;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.username());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciais invalidas"));
        }

        User user = userOptional.get();
        boolean isPasswordValid = passwordEncoder.matches(request.password(), user.getPassword());

        if (!isPasswordValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciais invalidas"));
        }

        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
        String token = jwtService.generateToken(user.getId(), user.getName(), user.getRole());
        LoginResponse response = new LoginResponse(token, jwtService.getExpirationTime(), userResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.accepted()
                .body(Map.of("message", "Se o email existir, as instrucoes de recuperacao serao enviadas"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso"));
    }
}
