package com.fluxo.report.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "report_review")
@PrimaryKeyJoinColumn(name = "report_id") // Faz o link com o ID da classe Report
@Getter @Setter @NoArgsConstructor
public class ReportReview extends Report {

    private String comment;

    @Column(name = "reference_url")
    private String referenceUrl;

    @Column(name = "review_date")
    private LocalDate reviewDate;
}