package com.fluxo.report.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "report_review")
@Getter @Setter @NoArgsConstructor
public class ReportReview {

    @Id
    @Column(name = "id_report")
    private Integer reportId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_report", referencedColumnName = "id_report", nullable = false)
    private Report report;

    @Column(nullable = false)
    private String comment;

    @Column(name = "correction_url", nullable = false)
    private String correctionUrl;

    @Column(name = "revision_date", nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime revisionDate;
}
