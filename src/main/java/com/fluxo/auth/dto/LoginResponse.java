package com.fluxo.auth.dto;

public record LoginResponse(
    String token, 
    long expiresIn, 
    UserResponse user
) {}