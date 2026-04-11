package com.fluxo.academic.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "class_session")
@Getter @Setter @NoArgsConstructor
public class ClassSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_session_id")
    private Integer id;

    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "class_group_id")
    private ClassGroup classGroup;

    @ManyToOne
    @JoinColumn(name = "attendance_id")
    private AttendanceRecord attendanceRecord;
}