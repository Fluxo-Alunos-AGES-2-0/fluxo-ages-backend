package com.fluxo.hours.entity;

import com.fluxo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "hours_request")
@Getter @Setter @NoArgsConstructor
public class HoursRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hours_request_id")
    private Integer id;

    private String activities;
    private Integer type;

    @Column(name = "hours_worked")
    private LocalTime hoursWorked;

    @Column(name = "hours_remaining")
    private LocalTime hoursRemaining;

    @Column(name = "entry_time")
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @ManyToOne
    @JoinColumn(name = "student_user_id", referencedColumnName = "user_id")
    private User studentUser;
}