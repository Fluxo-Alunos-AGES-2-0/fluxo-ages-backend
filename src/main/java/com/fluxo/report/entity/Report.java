package com.fluxo.report.entity;

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
    @Column(name = "report_id")
    private Integer id;

    private Integer type;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @Column(name = "edition_date")
    private LocalDate editionDate;

    @ManyToOne
    @JoinColumn(name = "student_user_id", referencedColumnName = "user_id")
    private User studentUser;
}