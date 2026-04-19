package com.fluxo.report.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "report_review")
@PrimaryKeyJoinColumn(name = "id_report")
@Getter @Setter @NoArgsConstructor
public class ReportReview extends Report {

    @Column(nullable = false)
    private String comment;

    @Column(name = "correction_url", nullable = false)
    private String correctionUrl;

    @Column(name = "revision_date", nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime revisionDate;
}
