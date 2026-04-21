package com.fluxo.hours.service;

import com.fluxo.hours.dto.ActiveHoursResponseDto;
import com.fluxo.hours.dto.HoursDTO;
import com.fluxo.hours.dto.StartHoursResponseDto;
import com.fluxo.hours.dto.StopHoursRequestDto;
import com.fluxo.hours.dto.StopHoursResponseDto;
import com.fluxo.hours.entity.HoursReport;
import com.fluxo.hours.exception.ActiveHoursNotFoundException;
import com.fluxo.hours.exception.HoursAlreadyOpenException;
import com.fluxo.hours.repository.HoursReportRepository;
import com.fluxo.project.entity.Project;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HoursService {

    private static final int HOURS_REPORT_TYPE = 1;
    private static final int DEFAULT_ACTIVITY_TYPE = 1;
    private static final long TOTAL_SECONDS = 216000L;

    private final HoursReportRepository hoursReportRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public StartHoursResponseDto startHours() {
        User authenticatedUser = getAuthenticatedUser();

        boolean alreadyHasOpenHours =
                hoursReportRepository.existsByStudentUserIdAndExitTimeIsNull(authenticatedUser.getId());

        if (alreadyHasOpenHours) {
            throw new HoursAlreadyOpenException(
                    "O aluno ja possui um registro de horas aberto."
            );
        }

        Project project = findCurrentProject(authenticatedUser.getId());

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        HoursReport hoursReport = new HoursReport();
        hoursReport.setType(HOURS_REPORT_TYPE);
        hoursReport.setCreateDate(now.toLocalDate());
        hoursReport.setEditDate(now.toLocalDate());
        hoursReport.setStudentUser(authenticatedUser);
        hoursReport.setProject(project);
        hoursReport.setActivityType(DEFAULT_ACTIVITY_TYPE);
        hoursReport.setEntryTime(now);

        HoursReport savedHoursReport = hoursReportRepository.save(hoursReport);

        return new StartHoursResponseDto(
                savedHoursReport.getId(),
                savedHoursReport.getEntryTime().toInstant()
        );
    }

    public StopHoursResponseDto stopHours(StopHoursRequestDto request) {
        User authenticatedUser = getAuthenticatedUser();

        HoursReport hoursReport = hoursReportRepository
                .findFirstByStudentUserIdAndExitTimeIsNullOrderByEntryTimeDesc(authenticatedUser.getId())
                .orElseThrow(() -> new ActiveHoursNotFoundException(
                        "Nao existe registro de horas aberto para o aluno autenticado."
                ));

        OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);
        int totalTimeSeconds = (int) Duration
                .between(hoursReport.getEntryTime().toInstant(), endTime.toInstant())
                .getSeconds();

        hoursReport.setActivities(request.description());
        hoursReport.setExitTime(endTime);
        hoursReport.setTotalTimeSeconds(totalTimeSeconds);
        hoursReport.setEditDate(LocalDate.now(ZoneOffset.UTC));

        HoursReport savedHoursReport = hoursReportRepository.save(hoursReport);

        return new StopHoursResponseDto(
                savedHoursReport.getId(),
                savedHoursReport.getActivities(),
                savedHoursReport.getEntryTime().toInstant(),
                savedHoursReport.getExitTime().toInstant(),
                savedHoursReport.getTotalTimeSeconds()
        );
    }

    public Optional<ActiveHoursResponseDto> findActiveHours() {
        User authenticatedUser = getAuthenticatedUser();

        return hoursReportRepository
                .findFirstByStudentUserIdAndExitTimeIsNullOrderByEntryTimeDesc(authenticatedUser.getId())
                .map(hoursReport -> new ActiveHoursResponseDto(
                        hoursReport.getId(),
                        hoursReport.getEntryTime().toInstant()
                ));
    }

    public List<StopHoursResponseDto> getMyHours() {
        User authenticatedUser = getAuthenticatedUser();

        return hoursReportRepository
                .findByStudentUserIdAndExitTimeIsNotNullOrderByEntryTimeDesc(authenticatedUser.getId())
                .stream()
                .map(hoursReport -> new StopHoursResponseDto(
                        hoursReport.getId(),
                        hoursReport.getActivities(),
                        hoursReport.getEntryTime().toInstant(),
                        hoursReport.getExitTime().toInstant(),
                        hoursReport.getTotalTimeSeconds()
                ))
                .toList();
    }

    public HoursDTO getHourControl() {
        User authenticatedUser = getAuthenticatedUser();

        long totalCompletedSeconds = getTotalSeconds(authenticatedUser.getId());
        long remainingSeconds = Math.max(0, TOTAL_SECONDS - totalCompletedSeconds);
        double percentual = (totalCompletedSeconds * 100.0) / TOTAL_SECONDS;

        return HoursDTO.builder()
                .completedSeconds(totalCompletedSeconds)
                .remainingSeconds(remainingSeconds)
                .totalSeconds(TOTAL_SECONDS)
                .percentual(percentual)
                .build();
    }

    public long getTotalSeconds(Integer userId) {
        return hoursReportRepository
                .findByStudentUserId(userId)
                .stream()
                .map(HoursReport::getTotalTimeSeconds)
                .filter(total -> total != null)
                .mapToLong(Integer::longValue)
                .sum();
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("Usuario autenticado nao encontrado.");
        }

        return user;
    }

    private Project findCurrentProject(Integer userId) {
        List<StudentProfile> result = entityManager.createQuery("""
            SELECT sp
            FROM StudentProfile sp
            WHERE sp.studentUser.id = :userId
            """, StudentProfile.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();

        if (result.isEmpty() || result.get(0).getTeam() == null || result.get(0).getTeam().getProject() == null) {
            throw new IllegalStateException("Projeto atual do aluno nao encontrado.");
        }

        return result.get(0).getTeam().getProject();
    }
}
