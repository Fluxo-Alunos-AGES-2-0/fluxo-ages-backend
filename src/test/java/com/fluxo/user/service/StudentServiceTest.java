package com.fluxo.user.service;

import com.fluxo.infra.storage.S3StorageService;
import com.fluxo.project.entity.ClassGroup;
import com.fluxo.project.entity.ClassSession;
import com.fluxo.user.dto.AttendanceHistoryResponseDto;
import com.fluxo.user.entity.AttendanceRecord;
import com.fluxo.user.entity.AttendanceRecordStatus;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.AttendanceRecordRepository;
import com.fluxo.user.repository.StudentProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private S3StorageService s3StorageService;

    @Test
    void getLoggedStudentAttendanceHistoryShouldGroupRecordsByDayAndPreserveStatus() {
        StudentService studentService = new StudentService(
                authenticatedUserService,
                studentProfileRepository,
                attendanceRecordRepository,
                s3StorageService
        );

        User user = createUser(1);

        AttendanceRecord firstSlot = createAttendanceRecord(
                "AT260313A1",
                AttendanceRecordStatus.PRESENTE,
                LocalDate.of(2026, 3, 13),
                "19:15 - 20:45"
        );
        AttendanceRecord secondSlot = createAttendanceRecord(
                "AT260313A2",
                AttendanceRecordStatus.PRESENTE,
                LocalDate.of(2026, 3, 13),
                "21:00 - 22:30"
        );
        AttendanceRecord thirdSlot = createAttendanceRecord(
                "AT260320B1",
                AttendanceRecordStatus.AUSENTE,
                LocalDate.of(2026, 3, 20),
                ""
        );

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
        when(attendanceRecordRepository.findDetailedByStudentUserId(user.getId()))
                .thenReturn(List.of(firstSlot, secondSlot, thirdSlot));

        AttendanceHistoryResponseDto response = studentService.getLoggedStudentAttendanceHistory();

        assertThat(response.days()).hasSize(2);
        assertThat(response.days().get(0).date()).isEqualTo("2026-03-13");
        assertThat(response.days().get(0).slots())
                .extracting(AttendanceHistoryResponseDto.AttendanceSlotDto::time,
                        AttendanceHistoryResponseDto.AttendanceSlotDto::status)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("19:15 - 20:45", "PRESENTE"),
                        org.assertj.core.groups.Tuple.tuple("21:00 - 22:30", "PRESENTE")
                );

        assertThat(response.days().get(1).date()).isEqualTo("2026-03-20");
        assertThat(response.days().get(1).slots())
                .extracting(AttendanceHistoryResponseDto.AttendanceSlotDto::time,
                        AttendanceHistoryResponseDto.AttendanceSlotDto::status)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Horário não informado", "AUSENTE")
                );
    }

    private User createUser(Integer id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private AttendanceRecord createAttendanceRecord(
            String id,
            AttendanceRecordStatus status,
            LocalDate date,
            String dateTime
    ) {
        ClassGroup classGroup = new ClassGroup();
        classGroup.setDate(date);
        classGroup.setDateTime(dateTime);

        ClassSession classSession = new ClassSession();
        classSession.setDate(date);
        classSession.setClassGroup(classGroup);

        AttendanceRecord attendanceRecord = new AttendanceRecord();
        attendanceRecord.setId(id);
        attendanceRecord.setStatus(status);
        attendanceRecord.setLessonSession(classSession);
        return attendanceRecord;
    }
}
