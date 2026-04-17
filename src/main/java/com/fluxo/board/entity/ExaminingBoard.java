package com.fluxo.board.entity;

import com.fluxo.user.entity.User;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "examining_board")
@Getter @Setter @NoArgsConstructor
public class ExaminingBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Integer id;

    private LocalDate date;

    // precision = 10 e scale = 2 reflete o decimal(10,2) do seu diagrama
    @Column(precision = 10, scale = 2)
    private BigDecimal grade;

    // Relacionamento apontando para o aluno que está sendo avaliado na banca
    @ManyToOne
    @JoinColumn(name = "student_user_id", referencedColumnName = "user_id")
    private User studentUser;
}