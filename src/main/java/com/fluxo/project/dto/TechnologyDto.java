package com.fluxo.project.dto;

public record TechnologyDto(
        Integer id, // Opcional, mas sempre bom mandar para o frontend usar como 'key' no React
        String name,
        String iconUrl
) {}