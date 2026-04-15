package com.fluxo.attendance.repository;

import com.fluxo.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByStudentUserIdAndEndTimeIsNull(Long userId);

    Optional<Attendance> findFirstByStudentUserIdAndEndTimeIsNullOrderByStartTimeDesc(Long userId);
}