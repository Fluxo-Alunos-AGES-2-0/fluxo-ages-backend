package com.fluxo.project.entity;

import com.fluxo.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project")
@Getter @Setter @NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Integer id;

    private String name;
    private String description;
    
    @Column(length = 10)
    private String status;
    
    @Column(length = 10)
    private String period;
    
    private String notes;
    private String technologies;
    private String practices;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
}