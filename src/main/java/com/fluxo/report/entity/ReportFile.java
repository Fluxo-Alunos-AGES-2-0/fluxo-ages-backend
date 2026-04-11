package com.fluxo.report.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "report_file")
@PrimaryKeyJoinColumn(name = "report_id")
@Getter @Setter @NoArgsConstructor
public class ReportFile extends Report {

    @Column(name = "file_url")
    private String fileUrl;
}