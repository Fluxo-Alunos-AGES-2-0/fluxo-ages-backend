package com.fluxo.report.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sprint_report")
@PrimaryKeyJoinColumn(name = "id_report")
@Getter @Setter @NoArgsConstructor
public class SprintReport extends Report {

    @Column(nullable = false, length = 2, columnDefinition = "char(2)")
    private String sprint;

    @Column(name = "predicted_activity", nullable = false)
    private String predictedActivity;

    @Column(name = "activity_completed", nullable = false)
    private String activityCompleted;

    @Column(name = "problems_encountered", nullable = false)
    private String problemsEncountered;

    @Column(name = "learned_lessons", nullable = false)
    private String learnedLessons;

    @Column(name = "next_steps", nullable = false)
    private String nextSteps;
}
