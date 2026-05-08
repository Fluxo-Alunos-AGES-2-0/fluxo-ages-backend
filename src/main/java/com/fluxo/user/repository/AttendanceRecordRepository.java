package com.fluxo.user.repository;

import com.fluxo.user.entity.AttendanceRecord;
import com.fluxo.user.entity.AttendanceRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, String> {
    int countByStudentUserIdAndStatus(Integer userId, AttendanceRecordStatus status);
    int countByStudentUserId(Integer userId);
}
