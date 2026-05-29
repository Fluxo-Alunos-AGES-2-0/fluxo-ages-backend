package com.fluxo.schedule.repository;

import com.fluxo.schedule.entity.ScheduleEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleEventRepository extends JpaRepository<ScheduleEvent, Integer> {
    List<ScheduleEvent> findByDiaTurno(String diaTurno);
    List<ScheduleEvent> findByDiaTurnoAndSprint(String diaTurno, Integer sprint);
}

