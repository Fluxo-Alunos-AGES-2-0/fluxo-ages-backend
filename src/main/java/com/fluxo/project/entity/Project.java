package com.fluxo.project.entity;
import java.util.Set;
import com.fluxo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false, length = 255)
    private String summary; // Descrição curta para o card

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl; // Ícone/capa do projeto

    @Column(name = "group_photo_url", length = 255)
    private String groupPhotoUrl; // Foto da equipe

    @ManyToMany
    @JoinTable(
    name = "project_technology",                 // Nome da tabela auxiliar no banco de dados
    joinColumns = @JoinColumn(name = "id_project"),          // Coluna que aponta para o Projeto dono
    inverseJoinColumns = @JoinColumn(name = "id_technology") // Coluna que aponta para a Tecnologia
    )
    private Set<Technology> technologies;
}
