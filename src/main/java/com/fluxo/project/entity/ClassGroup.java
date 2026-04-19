package com.fluxo.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "class")
@Getter
@Setter
@NoArgsConstructor
public class ClassGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_class")
    private Integer id;

    @Column(name = "date", nullable = false)
    private LocalDate date;
}
