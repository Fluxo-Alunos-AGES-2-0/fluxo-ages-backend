package com.fluxo.student.entity;

import jakarta.persistence.*;
import com.fluxo.user.entity.User;
import com.fluxo.team.entity.Team;

@Entity
@Table(name = "student_profile")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Integer id;

    @Column(name = "agpa_position")
    private String agpaPosition;

    @Column(length = 20)
    private String course;

    // Relacionamento com User: Vários perfis podem teoricamente existir, 
    // ou se for 1 para 1 exclusivo, troque para @OneToOne
    @ManyToOne
    @JoinColumn(name = "student_user_id", referencedColumnName = "user_id")
    private User studentUser;

    // Relacionamento com Team
    @ManyToOne
    @JoinColumn(name = "team_id", referencedColumnName = "team_id")
    private Team team;

    // Construtores, Getters e Setters
}