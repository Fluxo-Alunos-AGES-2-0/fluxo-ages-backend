package com.fluxo.team.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "team")
@Getter @Setter @NoArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Integer id;
}