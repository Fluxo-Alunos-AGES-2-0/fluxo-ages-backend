package com.fluxo.report.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hours_report")
@PrimaryKeyJoinColumn(name = "report_id") // Faz o link com o ID da classe Report
@Getter @Setter @NoArgsConstructor
public class HoursReport extends Report {

    @Column(length = 10)
    private String activities;

    @Column(name = "activity_type", length = 10)
    private String activityType;

    @Column(name = "entry_time")
    private LocalDate entryTime;

    @Column(name = "exit_time")
    private LocalDate exitTime;

    @Column(name = "total_hours")
    private LocalDateTime totalHours;

    @Column(name = "rejection_reason", length = 100)
    private String rejectionReason;
}