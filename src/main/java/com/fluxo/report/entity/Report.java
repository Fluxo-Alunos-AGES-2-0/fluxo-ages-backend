package com.fluxo.report.entity;

import com.fluxo.project.entity.Project;
import com.fluxo.report.enums.ReportType;
import com.fluxo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Inheritance(strategy = InheritanceType.JOINED) // Diz ao JPA que teremos tabelas filhas ligadas por ID
@Getter @Setter @NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_report")
    private Integer id;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ReportType type;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "edit_date", nullable = false)
    private LocalDateTime editDate;

    @Column(name = "grade", precision = 4, scale = 2)
    private BigDecimal grade;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user_student", referencedColumnName = "id_user", nullable = false)
    private User studentUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_project", referencedColumnName = "id_project", nullable = false)
    private Project project;
}
