package com.fluxo.schedule.entity;

import com.fluxo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "schedule")
@Getter @Setter @NoArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer id;

    @Column(length = 10)
    private String event;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "event_time")
    private LocalTime eventTime;

    @Column(name = "event_period", length = 10)
    private String eventPeriod;

    @ManyToOne
    @JoinColumn(name = "admin_user_id", referencedColumnName = "user_id")
    private User adminUser;
}