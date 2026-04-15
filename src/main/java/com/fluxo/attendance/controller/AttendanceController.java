package com.fluxo.attendance.controller;

import com.fluxo.attendance.dto.ActiveAttendanceResponseDto;
import com.fluxo.attendance.dto.StartAttendanceResponseDto;
import com.fluxo.attendance.dto.StopAttendanceRequestDto;
import com.fluxo.attendance.dto.StopAttendanceResponseDto;
import com.fluxo.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/start")
    public ResponseEntity<StartAttendanceResponseDto> startAttendance() {
        StartAttendanceResponseDto response = attendanceService.startAttendance();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/stop")
    public ResponseEntity<StopAttendanceResponseDto> stopAttendance(
            @RequestBody StopAttendanceRequestDto request
    ) {
        StopAttendanceResponseDto response = attendanceService.stopAttendance(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ActiveAttendanceResponseDto> getActiveAttendance() {
        return attendanceService.findActiveAttendance()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}