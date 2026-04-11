package com.fluxo.student.entity;

import com.fluxo.academic.entity.ClassGroup;
import com.fluxo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "student_history")
@Getter @Setter @NoArgsConstructor
public class StudentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_history_id")
    private Integer id;

    @Column(precision = 5, scale = 2)
    private BigDecimal grade;

    @Column(name = "year_semester")
    private Short yearSemester;

    @ManyToOne
    @JoinColumn(name = "class_group_id")
    private ClassGroup classGroup;

    @ManyToOne
    @JoinColumn(name = "student_user_id", referencedColumnName = "user_id")
    private User studentUser;
}