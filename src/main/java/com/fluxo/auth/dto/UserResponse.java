package com.fluxo.auth.dto;

public record UserResponse(
    Integer id,
    String name, 
    String email, 
    String role
) {}
