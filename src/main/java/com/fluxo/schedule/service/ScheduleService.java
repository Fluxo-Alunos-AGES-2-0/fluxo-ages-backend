package com.fluxo.schedule.service;

import com.fluxo.schedule.dto.ScheduleEventDto;
import com.fluxo.schedule.entity.ScheduleEvent;
import com.fluxo.schedule.repository.ScheduleEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleEventRepository repository;

    public List<ScheduleEventDto> listByDiaTurnoAndOptionalSprint(String diaTurno, Integer sprint) {
        List<ScheduleEvent> events;
        if (sprint == null) {
            events = repository.findByDiaTurno(diaTurno);
        } else {
            events = repository.findByDiaTurnoAndSprint(diaTurno, sprint);
        }

        return events.stream().map(this::toDto).collect(Collectors.toList());
    }

    private ScheduleEventDto toDto(ScheduleEvent e) {
        List<String> cats = e.getCategories() == null || e.getCategories().isBlank()
                ? List.of()
                : Arrays.stream(e.getCategories().split(",")).map(String::trim).collect(Collectors.toList());

        return new ScheduleEventDto(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getDate().toString(),
                e.getTime(),
                e.getSprint(),
                cats
        );
    }
}

