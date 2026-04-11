package com.fluxo.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_user") 
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "enrollment_number", nullable = false, length = 20)
    private String enrollmentNumber;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String role;

}