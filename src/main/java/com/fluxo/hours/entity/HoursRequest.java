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
    @Column(name = "id_hours_request")
    private Integer id;

    @Column(nullable = false)
    private String activities;

    @Column(name = "worked_hours", nullable = false)
    private LocalTime workedHours;

    @Column(name = "hours_remaining", nullable = false)
    private LocalTime hoursRemaining;

    @Column(name = "entry_hour", nullable = false)
    private LocalDateTime entryHour;

    @Column(name = "exit_hour", nullable = false)
    private LocalDateTime exitHour;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user_student", referencedColumnName = "id_user", nullable = false)
    private User studentUser;
}
