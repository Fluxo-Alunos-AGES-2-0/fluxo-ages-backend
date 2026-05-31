package com.fluxo.user.entity;

import com.fluxo.project.entity.Technology;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_technology")
@IdClass(StudentTechnologyId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentTechnology {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user_student", referencedColumnName = "id_user", nullable = false)
    private User studentUser;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_technology", referencedColumnName = "id_technology", nullable = false)
    private Technology technology;
}
