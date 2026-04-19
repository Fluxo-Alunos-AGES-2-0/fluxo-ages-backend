package com.fluxo.report.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "hours_report")
@PrimaryKeyJoinColumn(name = "id_report")
@Getter @Setter @NoArgsConstructor
public class HoursReport extends Report {

    @Column(nullable = false)
    private String activities;

    @Column(name = "activity_type", nullable = false)
    private Integer activityType;

    @Column(name = "entry_time", nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime entryTime;

    @Column(name = "exit_time", nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime exitTime;

    @Column(name = "total_time_seconds", nullable = false)
    private Integer totalTimeSeconds;

    @Column(name = "rejection_justification")
    private String rejectionJustification;
}
