package com.fluxo.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "iconUrl", length = 512)
    private String iconUrl;
    
}
