package com.fluxo.attendance.repository;

import com.fluxo.attendance.entity.Attendance;
import com.fluxo.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByStudentUserIdAndEndTimeIsNull(Integer userId);

    Optional<Attendance> findFirstByStudentUserIdAndEndTimeIsNullOrderByStartTimeDesc(Integer userId);
    List<Attendance> findByStudentUserIdAndStatus(Integer userId, AttendanceStatus status);
}
