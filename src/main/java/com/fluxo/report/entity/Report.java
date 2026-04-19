package com.fluxo.report.entity;

import com.fluxo.project.entity.Project;
import com.fluxo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "report")
@Inheritance(strategy = InheritanceType.JOINED) // Diz ao JPA que teremos tabelas filhas ligadas por ID
@Getter @Setter @NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_report")
    private Integer id;

    @Column(nullable = false)
    private Integer type;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "edit_date", nullable = false)
    private LocalDate editDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user_student", referencedColumnName = "id_user", nullable = false)
    private User studentUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_project", referencedColumnName = "id_project", nullable = false)
    private Project project;
}
