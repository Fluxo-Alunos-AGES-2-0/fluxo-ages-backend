package com.fluxo.report.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "hours_report")
@PrimaryKeyJoinColumn(name = "id_report")
@Getter
@Setter
@NoArgsConstructor
public class HoursReport extends Report {

    @Column
    private String activities;

    @Column(name = "activity_type")
    private Integer activityType;

    @Column(name = "entry_time", nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime entryTime;

    @Column(name = "exit_time", columnDefinition = "timestamp with time zone")
    private OffsetDateTime exitTime;

    @Column(name = "total_time_seconds")
    private Integer totalTimeSeconds;

    @Column(name = "rejection_justification")
    private String rejectionJustification;
}
