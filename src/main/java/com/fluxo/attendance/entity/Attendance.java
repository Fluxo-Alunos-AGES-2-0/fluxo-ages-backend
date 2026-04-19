package com.fluxo.attendance.entity;

import com.fluxo.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "horas_extraclasse")
@Getter
@Setter
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", referencedColumnName = "id_user", nullable = false)
    private User studentUser;

    @Column(name = "data_envio")
    private Instant submittedAt;

    @Column(name = "hora_entrada", nullable = false)
    private Instant startTime;

    @Column(name = "hora_saida")
    private Instant endTime;

    @Column(name = "total_horas_sessao")
    private Integer sessionTimeSeconds;

    @Column(name = "descricao")
    private String description;

    @Column(name = "observacao")
    private String observation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status;
}
