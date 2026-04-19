package com.fluxo.hours.controller;

import com.fluxo.hours.dto.ActiveHoursResponseDto;
import com.fluxo.hours.dto.StartHoursResponseDto;
import com.fluxo.hours.dto.StopHoursRequestDto;
import com.fluxo.hours.dto.StopHoursResponseDto;
import com.fluxo.hours.service.HoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hours")
@RequiredArgsConstructor
public class HoursController {

    private final HoursService hoursService;

    @PostMapping("/start")
    public ResponseEntity<StartHoursResponseDto> startHours() {
        StartHoursResponseDto response = hoursService.startHours();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/stop")
    public ResponseEntity<StopHoursResponseDto> stopHours(
            @RequestBody StopHoursRequestDto request
    ) {
        StopHoursResponseDto response = hoursService.stopHours(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ActiveHoursResponseDto> getActiveHours() {
        return hoursService.findActiveHours()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<List<StopHoursResponseDto>> getMyHours() {
        return ResponseEntity.ok(hoursService.getMyHours());
    }
}
