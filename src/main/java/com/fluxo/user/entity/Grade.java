package com.fluxo.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "grade")
@Getter
@Setter
@NoArgsConstructor
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grade")
    private Integer id;

    @Column(name = "\"value\"", nullable = false)
    private Integer value;

    @Column(nullable = false)
    private Integer type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user_student", referencedColumnName = "id_user", nullable = false)
    private User studentUser;
}
