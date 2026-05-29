package com.fluxo.schedule.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "schedule_event")
@Getter @Setter @NoArgsConstructor
public class ScheduleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_event")
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDate date;

    // free-form time range, e.g. "19:15 - 22:30"
    @Column(name = "time_range", nullable = false)
    private String time;

    @Column(name = "sprint")
    private Integer sprint;

    // stored as comma-separated values in DB for simplicity: "Evento,Orientação"
    @Column(name = "categories")
    private String categories;

    @Column(name = "dia_turno", nullable = false)
    private String diaTurno;
}

