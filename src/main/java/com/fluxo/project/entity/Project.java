package com.fluxo.project.entity;

import com.fluxo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "project")
@Getter @Setter @NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_project")
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, length = 10, columnDefinition = "char(10)")
    private String status;

    @Column(nullable = false, length = 10, columnDefinition = "char(10)")
    private String period;

    @Column(name = "observation")
    private String observation;

    @Column(name = "git_lab_link")
    private String gitLabLink;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user_teacher", referencedColumnName = "id_user", nullable = false)
    private User teacherUser;

    @Column(name = "summary")
    private String summary;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "group_photo_url")
    private String groupPhotoUrl;

    @Column(name = "semester_year")
    private String semesterYear;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "project_technology",
        joinColumns = @JoinColumn(name = "id_project"),
        inverseJoinColumns = @JoinColumn(name = "id_technology")
    )
    private Set<Technology> technologies;
}