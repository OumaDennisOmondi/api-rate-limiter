package com.irembo.apiratelimiter.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.time.Duration;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;

public enum TimeWindow{
    SECOND("s", ChronoField.SECOND_OF_MINUTE, "second"),
    MINUTE("m", ChronoField.MINUTE_OF_HOUR, "minute"),
    HOUR("h", ChronoField.HOUR_OF_DAY, "hour"),
    DAY("d", ChronoField.DAY_OF_MONTH, "day"),
    MONTH("M", ChronoField.MONTH_OF_YEAR, "month"),
    YEAR("y", ChronoField.YEAR, "year");

    private final String key;
    private final TemporalField temporalField;
    private final String name;

    TimeWindow(String key, TemporalField temporalField, String name) {
        this.key = key;
        this.temporalField = temporalField;
        this.name = name;
    }

    @JsonValue
    public String getKey() {
        return key;
    }

    public TemporalField getTemporalField() {
        return temporalField;
    }

    public Duration getDuration() {
        return temporalField.getBaseUnit().getDuration();
    }

    public String getName() {
        return name;
    }
}
