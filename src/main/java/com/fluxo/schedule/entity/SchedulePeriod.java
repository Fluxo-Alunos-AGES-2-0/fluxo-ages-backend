package com.fluxo.schedule.entity;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum SchedulePeriod {
    JK_SEGQUA("JK_SEGQUA"),
    LM_SEGQUA("LM_SEGQUA"),
    JK_TERQUI("JK_TERQUI"),
    LMNP_SEXTA("LMNP_SEXTA");

    private final String value;

    SchedulePeriod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<SchedulePeriod> fromValue(String value) {
        if (value == null) {
            return Optional.empty();
        }

        String normalizedValue = value.trim().toUpperCase(Locale.ROOT);

        return Arrays.stream(values())
                .filter(period -> period.value.equals(normalizedValue))
                .findFirst();
    }
}
