package com.fluxo.board.entity;

import com.fluxo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teacher_examining_board")
@Getter @Setter @NoArgsConstructor
public class TeacherExaminingBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_examining_board", referencedColumnName = "id", nullable = false)
    private ExaminingBoard examiningBoard;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user_teacher", referencedColumnName = "id_user", nullable = false)
    private User teacherUser;
}
