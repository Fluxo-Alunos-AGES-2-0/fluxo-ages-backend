package com.fluxo.auth.service;

import com.fluxo.auth.dto.LoginRequest;
import com.fluxo.auth.dto.LoginResponse;
import com.fluxo.auth.dto.UserResponse;
import com.fluxo.auth.exception.InvalidCredentialsException;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
