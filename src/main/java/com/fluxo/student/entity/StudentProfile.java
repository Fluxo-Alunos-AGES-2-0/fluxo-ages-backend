package com.fluxo.student.entity;

import com.fluxo.team.entity.Team;
import com.fluxo.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_profile")
@Getter
@Setter
@NoArgsConstructor
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profile")
    private Integer id;

    @Column(name = "ages_position", nullable = false)
    private Integer agesPosition;

    @Column(nullable = false)
    private String course;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user_student", referencedColumnName = "id_user", nullable = false)
    private User studentUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_team", referencedColumnName = "id_team", nullable = false)
    private Team team;
}
