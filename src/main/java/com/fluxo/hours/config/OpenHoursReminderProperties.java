package com.fluxo.hours.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.hours.open-reminder")
public class OpenHoursReminderProperties {

    private boolean enabled = false;
    private String secret = "";
    private Long thresholdMinutes;
    private long thresholdHours = 2;
    private long windowMinutes = 15;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getThresholdMinutes() {
        return thresholdMinutes;
    }

    public void setThresholdMinutes(Long thresholdMinutes) {
        this.thresholdMinutes = thresholdMinutes;
    }

    public long getThresholdHours() {
        return thresholdHours;
    }

    public void setThresholdHours(long thresholdHours) {
        this.thresholdHours = thresholdHours;
    }

    public long getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(long windowMinutes) {
        this.windowMinutes = windowMinutes;
    }

    public Duration reminderAge() {
        if (thresholdMinutes != null && thresholdMinutes > 0) {
            return Duration.ofMinutes(thresholdMinutes);
        }
        return Duration.ofHours(thresholdHours);
    }

    public Duration reminderWindow() {
        return Duration.ofMinutes(windowMinutes);
    }
}
