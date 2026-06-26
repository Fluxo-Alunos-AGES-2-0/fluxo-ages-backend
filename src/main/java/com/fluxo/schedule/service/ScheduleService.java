package com.fluxo.schedule.service;

import com.fluxo.schedule.entity.Schedule;
import com.fluxo.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public List<Schedule> listScheduleByPeriod(String eventPeriod) {
        return scheduleRepository.findByEventPeriodOrderByEventDateAscEventTimeAsc(eventPeriod);
    }
}
