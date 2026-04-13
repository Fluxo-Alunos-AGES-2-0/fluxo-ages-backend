package com.fluxo.schedule.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ScheduleController {

    @GetMapping("/schedule")
    public ResponseEntity<List<Map<String, String>>> listSchedule() {
        return ResponseEntity.ok(List.of());
    }
}
