package com.fluxo.auth.dto;

public record UserResponse(
    Long id, 
    String name, 
    String email, 
    String role
) {}