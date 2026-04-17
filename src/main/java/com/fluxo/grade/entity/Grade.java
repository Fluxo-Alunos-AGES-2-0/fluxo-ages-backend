package com.fluxo.grade.entity;

import com.fluxo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grade")
@Getter @Setter @NoArgsConstructor
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Integer id;

    private Integer value;
    private Integer type;

    @ManyToOne
    @JoinColumn(name = "student_user_id", referencedColumnName = "user_id")
    private User studentUser;
}