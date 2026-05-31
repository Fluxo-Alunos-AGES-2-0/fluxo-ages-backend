package com.fluxo.report.entity;

import com.fluxo.user.entity.StudentHistory;
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

    @Column(nullable = false)
    private Integer type;

    @Column(nullable = false)
    private Integer value;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_student_historic", referencedColumnName = "id_student_historic", nullable = false)
    private StudentHistory studentHistory;
}
