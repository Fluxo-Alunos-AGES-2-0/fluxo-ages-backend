package com.fluxo.report.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "report_archive")
@PrimaryKeyJoinColumn(name = "id_report")
@Getter @Setter @NoArgsConstructor
public class ReportArchive extends Report {

    @Column(name = "url_archive", nullable = false)
    private String urlArchive;
}
