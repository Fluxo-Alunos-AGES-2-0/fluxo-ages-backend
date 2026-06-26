package com.fluxo.user.service;

import com.fluxo.infra.storage.S3StorageService;
import com.fluxo.project.entity.Project;
import com.fluxo.user.dto.AttendanceHistoryResponseDto;
import com.fluxo.user.dto.AvatarUploadResponseDto;
import com.fluxo.user.dto.StudentProfileResponseDto;
import com.fluxo.user.entity.AttendanceRecord;
import com.fluxo.user.entity.AttendanceRecordStatus;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.entity.User;
import com.fluxo.user.exception.InvalidAvatarException;
import com.fluxo.user.repository.AttendanceRecordRepository;
import com.fluxo.user.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentService {

    private static final String S3_REFERENCE_PREFIX = "s3:";

    private final AuthenticatedUserService authenticatedUserService;
    private final StudentProfileRepository studentProfileRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final S3StorageService s3StorageService;

    public Optional<StudentProfileResponseDto> getLoggedStudentProfile() {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        StudentProfile studentProfile = findStudentProfileByUserId(authenticatedUser.getId());
        if (studentProfile == null || studentProfile.getTeam() == null || studentProfile.getTeam().getProject() == null) {
            return Optional.empty();
        }

        Project project = studentProfile.getTeam().getProject();

        int presences = countAttendanceByStatus(authenticatedUser.getId(), AttendanceRecordStatus.PRESENTE);
        int absences = countAttendanceByStatus(authenticatedUser.getId(), AttendanceRecordStatus.AUSENTE);
        int totalClasses = countAttendanceRecords(authenticatedUser.getId());

        String avatarUrl = studentProfile.getImageUrl();
        if (avatarUrl != null && avatarUrl.startsWith(S3_REFERENCE_PREFIX)) {
            try {
                avatarUrl = s3StorageService.createGetPresignedUrl(avatarUrl.substring(S3_REFERENCE_PREFIX.length()));
            } catch (Exception e) {
                // Keep original string if resolution fails
            }
        }

        Integer agesLevel = studentProfile.getAgesPosition();
        StudentProfileResponseDto.ProfessorDto professor = buildProfessorDto(project);

        return Optional.of(new StudentProfileResponseDto(
                authenticatedUser.getId(),
                authenticatedUser.getName(),
                authenticatedUser.getEmail(),
                avatarUrl,
                agesLevel,
                new StudentProfileResponseDto.CurrentProjectDto(
                        project.getId(),
                        project.getName()
                ),
                professor,
                new StudentProfileResponseDto.AttendanceDto(
                        totalClasses,
                        presences,
                        absences
                )
        ));
    }

    public AttendanceHistoryResponseDto getLoggedStudentAttendanceHistory() {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();
        List<AttendanceRecord> records = attendanceRecordRepository.findDetailedByStudentUserId(authenticatedUser.getId());

        Map<LocalDate, List<AttendanceHistoryResponseDto.AttendanceSlotDto>> groupedByDate = new LinkedHashMap<>();

        for (AttendanceRecord record : records) {
            LocalDate date = record.getLessonSession().getDate();
            String time = resolveAttendanceTime(record);

            groupedByDate.computeIfAbsent(date, ignored -> new ArrayList<>())
                    .add(new AttendanceHistoryResponseDto.AttendanceSlotDto(
                            time,
                            record.getStatus().name()
                    ));
        }

        List<AttendanceHistoryResponseDto.AttendanceDayDto> days = groupedByDate.entrySet()
                .stream()
                .map(entry -> new AttendanceHistoryResponseDto.AttendanceDayDto(
                        entry.getKey().toString(),
                        entry.getValue()
                ))
                .toList();

        return new AttendanceHistoryResponseDto(days);
    }

    private StudentProfile findStudentProfileByUserId(Integer userId) {
        return studentProfileRepository.findByStudentUserId(userId).orElse(null);
    }

    private StudentProfileResponseDto.ProfessorDto buildProfessorDto(Project project) {
        if (project.getTeacherUser() == null) {
            return null;
        }

        return new StudentProfileResponseDto.ProfessorDto(
                project.getTeacherUser().getId(),
                project.getTeacherUser().getName()
        );
    }

    private int countAttendanceByStatus(Integer userId, AttendanceRecordStatus status) {
        return attendanceRecordRepository.countByStudentUserIdAndStatus(userId, status);
    }

    private int countAttendanceRecords(Integer userId) {
        return attendanceRecordRepository.countByStudentUserId(userId);
    }

    private String resolveAttendanceTime(AttendanceRecord record) {
        String dateTime = record.getLessonSession().getClassGroup().getDateTime();
        if (dateTime == null || dateTime.isBlank()) {
            return "Horário não informado";
        }

        return dateTime.trim();
    }

    public AvatarUploadResponseDto updateAvatar(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidAvatarException("O arquivo de imagem não pode estar vazio.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new InvalidAvatarException("A imagem não pode ter mais de 5MB.");
        }

        String contentType = file.getContentType();
        Set<String> allowedContentTypes = Set.of("image/jpeg", "image/png", "image/webp");

        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            throw new InvalidAvatarException("Formato de imagem inválido. Apenas JPG, PNG e WEBP são permitidos.");
        }

        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();
        StudentProfile studentProfile = findStudentProfileByUserId(authenticatedUser.getId());
        if (studentProfile == null) {
            throw new RuntimeException("Perfil de estudante não encontrado.");
        }

        String key = s3StorageService.buildKey("avatar/" + authenticatedUser.getId() + "/avatar");

        try {
            s3StorageService.uploadObject(key, contentType, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo de imagem.", e);
        }

        studentProfile.setImageUrl(S3_REFERENCE_PREFIX + key);
        studentProfileRepository.save(studentProfile);

        String presignedUrl = s3StorageService.createGetPresignedUrl(key);
        return new AvatarUploadResponseDto(presignedUrl);
    }
}
