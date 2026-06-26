package com.fluxo.project.service;

import com.fluxo.infra.storage.S3StorageService;
import com.fluxo.project.entity.Project;
import com.fluxo.project.entity.ProjectStatus;
import com.fluxo.project.repository.ProjectRepository;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.repository.StudentHistoryRepository;
import com.fluxo.user.repository.StudentProfileRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private StudentHistoryRepository studentHistoryRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private S3StorageService s3StorageService;

    @InjectMocks
    private ProjectService projectService;

    @Test
    @DisplayName("updateProject stores thumbnail in a dedicated folder and removes the previous file when the key changes")
    void updateProjectStoresThumbnailInDedicatedFolderAndRemovesPreviousFile() throws IOException {
        Project project = buildEditableProject();
        project.setThumbnailUrl("s3:dev/projects/10/thumbnail/thumbnail.webp");

        StudentProfile profile = new StudentProfile();
        profile.setAgesPosition(4);

        MultipartFile thumbnail = buildMultipartFile("image/png", new byte[]{1, 2, 3});

        when(projectRepository.findById(10)).thenReturn(Optional.of(project));
        when(studentProfileRepository.findByStudentUserIdAndTeamProjectId(7, 10)).thenReturn(Optional.of(profile));
        when(authenticatedUserService.getUserId()).thenReturn(7);
        when(s3StorageService.buildKey(anyString())).thenAnswer(invocation -> "dev/" + invocation.getArgument(0));
        when(projectRepository.save(project)).thenReturn(project);

        projectService.updateProject(10, null, null, thumbnail, null);

        assertEquals("s3:dev/projects/10/thumbnail/thumbnail.png", project.getThumbnailUrl());
        verify(s3StorageService).buildKey("projects/10/thumbnail/thumbnail.png");
        verify(s3StorageService).uploadObject(
                "dev/projects/10/thumbnail/thumbnail.png",
                "image/png",
                new byte[]{1, 2, 3}
        );
        verify(s3StorageService).deleteObject("dev/projects/10/thumbnail/thumbnail.webp");
    }

    @Test
    @DisplayName("updateProject stores group photo in a dedicated folder and keeps a single file when the key is unchanged")
    void updateProjectStoresGroupPhotoInDedicatedFolderAndDoesNotDeleteWhenKeyIsUnchanged() throws IOException {
        Project project = buildEditableProject();
        project.setGroupPhotoUrl("s3:dev/projects/10/groupPhoto/groupPhoto.webp");

        StudentProfile profile = new StudentProfile();
        profile.setAgesPosition(4);

        MultipartFile groupPhoto = buildMultipartFile("image/webp", new byte[]{9, 8, 7});

        when(projectRepository.findById(10)).thenReturn(Optional.of(project));
        when(studentProfileRepository.findByStudentUserIdAndTeamProjectId(7, 10)).thenReturn(Optional.of(profile));
        when(authenticatedUserService.getUserId()).thenReturn(7);
        when(s3StorageService.buildKey(anyString())).thenAnswer(invocation -> "dev/" + invocation.getArgument(0));
        when(projectRepository.save(project)).thenReturn(project);

        projectService.updateProject(10, null, null, null, groupPhoto);

        assertEquals("s3:dev/projects/10/groupPhoto/groupPhoto.webp", project.getGroupPhotoUrl());
        verify(s3StorageService).buildKey("projects/10/groupPhoto/groupPhoto.webp");
        verify(s3StorageService).uploadObject(
                "dev/projects/10/groupPhoto/groupPhoto.webp",
                "image/webp",
                new byte[]{9, 8, 7}
        );
        verify(s3StorageService, never()).deleteObject(anyString());
    }

    private Project buildEditableProject() {
        Project project = new Project();
        project.setId(10);
        project.setStatus(ProjectStatus.EM_ANDAMENTO);
        return project;
    }

    private MultipartFile buildMultipartFile(String contentType, byte[] bytes) throws IOException {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(contentType);
        when(file.getBytes()).thenReturn(bytes);
        return file;
    }
}
