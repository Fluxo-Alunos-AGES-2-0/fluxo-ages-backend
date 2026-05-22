package com.fluxo.user.entity;

import com.fluxo.project.entity.ClassSession;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "attendance_record")
@Getter
@Setter
@NoArgsConstructor
public class AttendanceRecord {

    @Id
    @Column(name = "id_attendance", nullable = false, length = 10, columnDefinition = "char(10)")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AttendanceRecordStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user_student", referencedColumnName = "id_user", nullable = false)
    private User studentUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_lesson_session", referencedColumnName = "id_lesson_session", nullable = false)
    private ClassSession lessonSession;

    @PrePersist
    private void assignId() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 10)
                    .toUpperCase();
        }
    }
}
