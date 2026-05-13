package com.fluxo.report.dto;

import com.fluxo.report.entity.HoursReport;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class HoursReportDto {

    private Long id_report;
    private String date;
    private String entry_time;
    private String exit_time;
    private String total_hours;
    private String activities;
    private String status;

    public HoursReportDto(HoursReport report) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        OffsetDateTime entry = report.getEntryTime();
        OffsetDateTime exit = report.getExitTime();

        this.id_report = report.getId().longValue();
        this.date = entry != null ? entry.format(dateFormat) : null;
        this.entry_time = entry != null ? entry.format(timeFormat) : null;
        this.exit_time = exit != null ? exit.format(timeFormat) : null;

        this.total_hours = formatSeconds(report.getTotalTimeSeconds());
        this.activities = report.getActivities();
        this.status = mapStatus(report);
    }

    private String formatSeconds(Integer seconds) {
        if (seconds == null) return null;

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        return String.format("%02d:%02d", hours, minutes);
    }

    private String mapStatus(HoursReport report) {
        if (report.getRejectionJustification() != null && !report.getRejectionJustification().isBlank()) {
            return "REJECTED";
        }
        if (report.getExitTime() == null) {
            return "PENDING";
        }
        return "APPROVED";
    }

    public Long getId_report() { return id_report; }
    public String getDate() { return date; }
    public String getEntry_time() { return entry_time; }
    public String getExit_time() { return exit_time; }
    public String getTotal_hours() { return total_hours; }
    public String getActivities() { return activities; }
    public String getStatus() { return status; }
}