package com.fluxo.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_users") // Usamos tb_users porque 'user' é uma palavra reservada no Postgres
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String name;
    private String email;
    private String role;

    // Construtor vazio (Obrigatório para o JPA)
    public User() {}

    // Construtor com parâmetros para facilitar a criação
    public User(String username, String password, String name, String email, String role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}