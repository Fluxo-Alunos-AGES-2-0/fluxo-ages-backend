package com.fluxo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "username") // Mudei para username pois 'user' é palavra reservada no SQL
    private String user;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    private String senha;

    @Column(columnDefinition = "TEXT") // Avatar costuma ser uma URL longa ou Base64
    private String avatar;

    @Enumerated(EnumType.STRING) // Salva o texto 'ALUNO' no banco em vez do número 0
    @Column(nullable = false)
    private TipoUsuario tipo;

    @CreationTimestamp // O Hibernate preenche a data sozinho na hora do insert
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}