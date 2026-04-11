package com.fluxo.board.entity;

import com.fluxo.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "examining_board_professor")
@Getter @Setter @NoArgsConstructor
public class ExaminingBoardProfessor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "board_id", referencedColumnName = "board_id")
    private ExaminingBoard examiningBoard;

    // Relacionamento apontando para qual Professor está participando da banca
    @ManyToOne
    @JoinColumn(name = "professor_user_id", referencedColumnName = "user_id")
    private User professorUser;
}