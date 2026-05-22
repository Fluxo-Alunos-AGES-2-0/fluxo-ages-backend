package com.fluxo.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluxo.auth.dto.ForgotPasswordRequest;
import com.fluxo.auth.dto.LoginRequest;
import com.fluxo.auth.dto.LoginResponse;
import com.fluxo.auth.dto.ResetPasswordRequest;
import com.fluxo.auth.dto.UserResponse;
import com.fluxo.auth.exception.InvalidCredentialsException;
import com.fluxo.auth.service.AuthService;
import com.fluxo.infra.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(authService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void loginShouldReturnOkWhenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest("Aluno", "senha123");

        UserResponse userResponse = new UserResponse(
                1,
                "Aluno",
                "aluno@email.com",
                "STUDENT"
        );

        LoginResponse response = new LoginResponse(
                "jwt-token",
                3600L,
                userResponse
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.name").value("Aluno"))
                .andExpect(jsonPath("$.user.email").value("aluno@email.com"))
                .andExpect(jsonPath("$.user.role").value("STUDENT"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest("Aluno", "senhaErrada");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Credenciais invalidas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciais invalidas"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void forgotPasswordShouldReturnOkWhenEmailIsValid() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("aluno@email.com");

        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Se o email existir, as instrucoes de recuperacao serao enviadas"));

        verify(authService).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void forgotPasswordShouldReturnOkWhenEmailDoesNotExist() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("naoexiste@email.com");

        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Se o email existir, as instrucoes de recuperacao serao enviadas"));

        verify(authService).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void forgotPasswordShouldReturnBadRequestWhenEmailIsEmpty() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email é obrigatório"));

        verify(authService, never()).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void resetPasswordShouldReturnOkWhenRequestIsValid() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest(
                "valid-token",
                "novaSenha123"
        );

        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha redefinida com sucesso"));

        verify(authService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    void resetPasswordShouldReturnBadRequestWhenTokenIsInvalidOrExpired() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest(
                "invalid-token",
                "novaSenha123"
        );

        doThrow(new RuntimeException("Link de recuperação inválido ou expirado"))
                .when(authService)
                .resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Link de recuperação inválido ou expirado"));

        verify(authService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    void resetPasswordShouldReturnBadRequestWhenPasswordIsTooShort() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest(
                "valid-token",
                "123"
        );

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Senha deve ter no mínimo 8 caracteres"));

        verify(authService, never()).resetPassword(any(ResetPasswordRequest.class));
    }
}