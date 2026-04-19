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
    @Column(name = "id_schedule")
    private Integer id;

    @Column(nullable = false, length = 10, columnDefinition = "char(10)")
    private String event;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_time", nullable = false)
    private LocalTime eventTime;

    @Column(name = "event_period", nullable = false, length = 10, columnDefinition = "char(10)")
    private String eventPeriod;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user_admin", referencedColumnName = "id_user", nullable = false)
    private User adminUser;
}
