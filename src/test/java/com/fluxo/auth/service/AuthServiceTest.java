package com.fluxo.auth.service;

import com.fluxo.auth.dto.ForgotPasswordRequest;
import com.fluxo.auth.dto.LoginRequest;
import com.fluxo.auth.dto.LoginResponse;
import com.fluxo.auth.dto.ResetPasswordRequest;
import com.fluxo.auth.exception.InvalidCredentialsException;
import com.fluxo.user.entity.PasswordResetToken;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.PasswordResetTokenRepository;
import com.fluxo.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldReturnTokenAndUserDataWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("Aluno", "senha123");

        User user = createUser();

        when(userRepository.findByIdentifierIgnoreCase("Aluno")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user.getId(), user.getName(), user.getRole())).thenReturn("jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        LoginResponse response = authService.login(request);

        assertEquals("jwt-token", response.token());
        assertEquals(3600L, response.expiresIn());
        assertEquals(user.getId(), response.user().id());
        assertEquals(user.getName(), response.user().name());
        assertEquals(user.getEmail(), response.user().email());
        assertEquals(user.getRole(), response.user().role());

        verify(userRepository).findByIdentifierIgnoreCase("Aluno");
        verify(passwordEncoder).matches("senha123", user.getPassword());
        verify(jwtService).generateToken(user.getId(), user.getName(), user.getRole());
    }

    @Test
    void loginShouldThrowInvalidCredentialsExceptionWhenIdentifierIsBlank() {
        LoginRequest request = new LoginRequest(" ", "senha123");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        verify(userRepository, never()).findByIdentifierIgnoreCase(any());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    void loginShouldThrowInvalidCredentialsExceptionWhenUserIsNotFound() {
        LoginRequest request = new LoginRequest("naoexiste@email.com", "senha123");

        when(userRepository.findByIdentifierIgnoreCase("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        verify(userRepository).findByIdentifierIgnoreCase("naoexiste@email.com");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    void loginShouldThrowInvalidCredentialsExceptionWhenPasswordIsIncorrect() {
        LoginRequest request = new LoginRequest("Aluno", "senhaErrada");

        User user = createUser();

        when(userRepository.findByIdentifierIgnoreCase("Aluno")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaErrada", user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        verify(userRepository).findByIdentifierIgnoreCase("Aluno");
        verify(passwordEncoder).matches("senhaErrada", user.getPassword());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    void forgotPasswordShouldReturnSilentlyWhenEmailDoesNotExist() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("naoexiste@email.com");

        when(userRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        authService.forgotPassword(request);

        verify(userRepository).findByEmail("naoexiste@email.com");
        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void forgotPasswordShouldPersistTokenAndSendEmailWhenEmailExists() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("aluno@email.com");

        User user = createUser();

        when(userRepository.findByEmail("aluno@email.com")).thenReturn(Optional.of(user));

        authService.forgotPassword(request);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);

        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        verify(emailService).sendPasswordResetEmail(eq("aluno@email.com"), any(String.class));

        PasswordResetToken savedToken = tokenCaptor.getValue();

        assertEquals(user, savedToken.getUser());
        assertNotNull(savedToken.getToken());
        assertNotNull(savedToken.getCreatedAt());
        assertNotNull(savedToken.getExpiresAt());
        assertTrue(savedToken.getExpiresAt().isAfter(savedToken.getCreatedAt()));
    }

    @Test
    void resetPasswordShouldThrowExceptionWhenTokenIsInvalid() {
        ResetPasswordRequest request = new ResetPasswordRequest("invalid-token", "novaSenha123");

        when(passwordResetTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.resetPassword(request));

        verify(userRepository, never()).save(any());
        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void resetPasswordShouldThrowExceptionWhenTokenIsAlreadyUsed() {
        ResetPasswordRequest request = new ResetPasswordRequest("used-token", "novaSenha123");

        PasswordResetToken token = createValidPasswordResetToken();
        token.setUsed(true);

        when(passwordResetTokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

        assertThrows(BadCredentialsException.class, () -> authService.resetPassword(request));

        verify(userRepository, never()).save(any());
        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void resetPasswordShouldThrowExceptionWhenTokenIsExpired() {
        ResetPasswordRequest request = new ResetPasswordRequest("expired-token", "novaSenha123");

        PasswordResetToken token = createValidPasswordResetToken();
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThrows(BadCredentialsException.class, () -> authService.resetPassword(request));

        verify(userRepository, never()).save(any());
        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void resetPasswordShouldThrowExceptionWhenNewPasswordIsTooShort() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "123");

        PasswordResetToken token = createValidPasswordResetToken();

        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));

        verify(userRepository, never()).save(any());
        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void resetPasswordShouldUpdatePasswordAndMarkTokenAsUsedWhenTokenIsValid() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "novaSenha123");

        PasswordResetToken token = createValidPasswordResetToken();
        User user = token.getUser();

        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("encoded-password");

        authService.resetPassword(request);

        assertEquals("encoded-password", user.getPassword());
        assertTrue(token.isUsed());

        verify(passwordEncoder).encode("novaSenha123");
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).save(token);
    }

    private User createUser() {
        User user = new User();
        user.setId(1);
        user.setName("Aluno");
        user.setEmail("aluno@email.com");
        user.setPassword("encoded-current-password");
        user.setRole("STUDENT");
        user.setEnrollmentNumber("123456");
        return user;
    }

    private PasswordResetToken createValidPasswordResetToken() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("valid-token");
        token.setUser(createUser());
        token.setUsed(false);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        return token;
    }
}
