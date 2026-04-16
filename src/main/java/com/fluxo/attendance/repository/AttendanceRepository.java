package com.fluxo.attendance.repository;

import com.fluxo.attendance.entity.Attendance;
import com.fluxo.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByStudentUserIdAndEndTimeIsNull(Long userId);

    Optional<Attendance> findFirstByStudentUserIdAndEndTimeIsNullOrderByStartTimeDesc(Long userId);
    List<Attendance> findByStudentUserIdAndStatus(Long userId, AttendanceStatus status);
}