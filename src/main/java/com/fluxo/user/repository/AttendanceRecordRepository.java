package com.fluxo.user.repository;

import com.fluxo.user.entity.AttendanceRecord;
import com.fluxo.user.entity.AttendanceRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, String> {
    int countByStudentUserIdAndStatus(Integer userId, AttendanceRecordStatus status);
    int countByStudentUserId(Integer userId);

    @Query("""
            select attendanceRecord
            from AttendanceRecord attendanceRecord
            join fetch attendanceRecord.lessonSession lessonSession
            join fetch lessonSession.classGroup classGroup
            where attendanceRecord.studentUser.id = :userId
            order by lessonSession.date asc, classGroup.dateTime asc, attendanceRecord.id asc
            """)
    List<AttendanceRecord> findDetailedByStudentUserId(Integer userId);
}
