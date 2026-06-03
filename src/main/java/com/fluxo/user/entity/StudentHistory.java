package com.fluxo.user.entity;

import com.fluxo.project.entity.ClassGroup;
import com.fluxo.project.entity.Project;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "student_historic")
@Getter
@Setter
@NoArgsConstructor
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

    @Enumerated(EnumType.STRING)
    @Column(name = "student_status", nullable = false, length = 20)
    private StudentStatus studentStatus = StudentStatus.REGULAR;

    @Column(name = "ages_level", nullable = false)
    private Integer agesLevel = 1;
}
