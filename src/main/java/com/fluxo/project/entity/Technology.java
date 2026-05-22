package com.fluxo.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Entity
@Table(name = "technology")
@Getter
@Setter
@NoArgsConstructor
public class Technology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_technology")
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // Ex: "React", "TypeScript", "Spring Boot"

    @ManyToMany(mappedBy = "technologies")
    private Set<Project> projects;
}