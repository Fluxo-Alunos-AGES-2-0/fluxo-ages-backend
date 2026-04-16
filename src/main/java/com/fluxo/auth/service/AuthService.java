package com.fluxo.auth.service;

import com.fluxo.auth.dto.ForgotPasswordRequest;
import com.fluxo.auth.dto.LoginRequest;
import com.fluxo.auth.dto.LoginResponse;
import com.fluxo.auth.dto.ResetPasswordRequest;
import com.fluxo.auth.dto.UserResponse;
import com.fluxo.auth.exception.InvalidCredentialsException;
import com.fluxo.user.entity.User;
import com.fluxo.user.entity.PasswordResetToken;
import com.fluxo.user.repository.UserRepository;

import com.fluxo.user.repository.PasswordResetTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public AuthService(JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordResetTokenRepository passwordResetTokenRepository, EmailService emailService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais invalidas"));

        boolean isPasswordValid = passwordEncoder.matches(request.password(), user.getPassword());

        if (!isPasswordValid) {
            throw new InvalidCredentialsException("Credenciais invalidas");
        }

        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
        String token = jwtService.generateToken(user.getId(), user.getName(), user.getRole());

        return new LoginResponse(token, jwtService.getExpirationTime(), userResponse);
    }

    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        if (forgotPasswordRequest.getEmail() == null || forgotPasswordRequest.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Digite seu e-mail.");
        }

        User user = userRepository.findByUsername(forgotPasswordRequest.getEmail()).orElseThrow(
            () -> new IllegalArgumentException("E-mail não cadastrado.")
        );

        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        passwordResetToken.setCreatedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(passwordResetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        if (resetPasswordRequest.getNewPassword() == null || resetPasswordRequest.getNewPassword().isEmpty()) {
            throw new IllegalArgumentException("A nova senha é obrigatória.");
        }

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(resetPasswordRequest.getToken()).orElse(null);

        if (passwordResetToken == null || passwordResetToken.isUsed() || passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Link de recuperação inválido ou expirado");
        }

        // Validação da força da senha (exemplo simples)
        if (resetPasswordRequest.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 8 caracteres.");
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        userRepository.save(user);

        passwordResetToken.setUsed(true);
        passwordResetTokenRepository.save(passwordResetToken);
    }
}
