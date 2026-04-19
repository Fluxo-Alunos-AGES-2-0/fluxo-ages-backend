package com.fluxo.team.entity;

import com.fluxo.project.entity.Project;
import com.fluxo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "team")
@Getter @Setter @NoArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_team")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_project", referencedColumnName = "id_project", nullable = false)
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user_teacher", referencedColumnName = "id_user", nullable = false)
    private User teacherUser;
}
