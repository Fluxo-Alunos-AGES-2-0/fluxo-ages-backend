package com.fluxo.student.entity;

import com.fluxo.academic.entity.ClassGroup;
import com.fluxo.project.entity.Project;
import com.fluxo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "student_historic")
@Getter @Setter @NoArgsConstructor
public class StudentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_student_historic")
    private Integer id;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal grade;

    @Column(name = "semester_year", nullable = false)
    private Short semesterYear;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_class", referencedColumnName = "id_class", nullable = false)
    private ClassGroup classGroup;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user_student", referencedColumnName = "id_user", nullable = false)
    private User studentUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_project", referencedColumnName = "id_project", nullable = false)
    private Project project;
}
