package com.fluxo.report.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "sprint_report")
@PrimaryKeyJoinColumn(name = "report_id") // Faz o link com o ID da classe Report
@Getter @Setter @NoArgsConstructor
public class SprintReport extends Report {

    @Column(length = 10)
    private String sprint;

    @Column(name = "planned_activity")
    private String plannedActivity;

    @Column(name = "completed_activity")
    private String completedActivity;

    @Column(name = "problems_encountered")
    private String problemsEncountered;

    @Column(name = "lessons_learned")
    private String lessonsLearned;

    @Column(name = "next_steps")
    private String nextSteps;

    @Column(name = "sprint_end_date")
    private LocalDate sprintEndDate;
}