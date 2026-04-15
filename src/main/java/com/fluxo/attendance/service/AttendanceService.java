package com.fluxo.attendance.service;

import com.fluxo.attendance.dto.ActiveAttendanceResponseDto;
import com.fluxo.attendance.dto.StartAttendanceResponseDto;
import com.fluxo.attendance.dto.StopAttendanceRequestDto;
import com.fluxo.attendance.dto.StopAttendanceResponseDto;
import com.fluxo.attendance.entity.Attendance;
import com.fluxo.attendance.entity.AttendanceStatus;
import com.fluxo.attendance.exception.ActiveAttendanceNotFoundException;
import com.fluxo.attendance.exception.AttendanceAlreadyOpenException;
import com.fluxo.attendance.repository.AttendanceRepository;
import com.fluxo.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public StartAttendanceResponseDto startAttendance() {
        User authenticatedUser = getAuthenticatedUser();

        boolean alreadyHasOpenAttendance =
                attendanceRepository.existsByStudentUserIdAndEndTimeIsNull(authenticatedUser.getId());

        if (alreadyHasOpenAttendance) {
            throw new AttendanceAlreadyOpenException(
                    "O aluno já possui um registro de ponto aberto."
            );
        }

        Attendance attendance = new Attendance();
        attendance.setStudentUser(authenticatedUser);
        attendance.setStartTime(Instant.now());
        attendance.setStatus(AttendanceStatus.PENDENTE);

        Attendance savedAttendance = attendanceRepository.save(attendance);

        return new StartAttendanceResponseDto(
                savedAttendance.getId(),
                savedAttendance.getStartTime(),
                savedAttendance.getStatus()
        );
    }

    public StopAttendanceResponseDto stopAttendance(StopAttendanceRequestDto request) {
        User authenticatedUser = getAuthenticatedUser();

        Attendance attendance = attendanceRepository
                .findFirstByStudentUserIdAndEndTimeIsNullOrderByStartTimeDesc(authenticatedUser.getId())
                .orElseThrow(() -> new ActiveAttendanceNotFoundException(
                        "Não existe registro de ponto aberto para o aluno autenticado."
                ));

        Instant endTime = Instant.now();
        int sessionTimeSeconds = (int) Duration
                .between(attendance.getStartTime(), endTime)
                .getSeconds();

        attendance.setEndTime(endTime);
        attendance.setDescription(request.description());
        attendance.setSessionTimeSeconds(sessionTimeSeconds);
        attendance.setSubmittedAt(endTime);
        attendance.setStatus(AttendanceStatus.PENDENTE);

        Attendance savedAttendance = attendanceRepository.save(attendance);

        return new StopAttendanceResponseDto(
                savedAttendance.getId(),
                savedAttendance.getDescription(),
                savedAttendance.getStartTime(),
                savedAttendance.getEndTime(),
                savedAttendance.getSessionTimeSeconds(),
                savedAttendance.getStatus()
        );
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("Usuário autenticado não encontrado.");
        }

        return user;
    }

    public Optional<ActiveAttendanceResponseDto> findActiveAttendance() {
        User authenticatedUser = getAuthenticatedUser();

        return attendanceRepository
                .findFirstByStudentUserIdAndEndTimeIsNullOrderByStartTimeDesc(authenticatedUser.getId())
                .map(attendance -> new ActiveAttendanceResponseDto(
                        attendance.getId(),
                        attendance.getStartTime()
                ));
    }
}