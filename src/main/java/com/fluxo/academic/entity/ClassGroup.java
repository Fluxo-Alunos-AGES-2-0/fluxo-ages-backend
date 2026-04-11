package com.fluxo.academic.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "class_group")
@Getter @Setter @NoArgsConstructor
public class ClassGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_group_id")
    private Integer id;

    @Column(name = "class_date")
    private LocalDate classDate;
}