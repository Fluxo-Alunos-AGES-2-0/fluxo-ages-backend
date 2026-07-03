package com.fluxo.hours.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class OpenHoursReminderPropertiesTest {

    @Test
    void reminderAgeShouldUseThresholdHoursByDefault() {
        OpenHoursReminderProperties properties = new OpenHoursReminderProperties();
        properties.setThresholdHours(2);

        assertThat(properties.reminderAge()).isEqualTo(Duration.ofHours(2));
    }

    @Test
    void reminderAgeShouldPreferThresholdMinutesWhenConfigured() {
        OpenHoursReminderProperties properties = new OpenHoursReminderProperties();
        properties.setThresholdHours(2);
        properties.setThresholdMinutes(5L);

        assertThat(properties.reminderAge()).isEqualTo(Duration.ofMinutes(5));
    }
}
