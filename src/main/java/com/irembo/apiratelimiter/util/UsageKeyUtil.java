package com.irembo.apiratelimiter.util;

import com.irembo.apiratelimiter.model.TimeWindow;
import java.time.Instant;
import java.time.ZoneOffset;

public class UsageKeyUtil {

    public static String timedKey(String prefix, TimeWindow timeWindow, Instant now) {
        return prefix + ":" + timeWindow.getKey() + ":" + now.atOffset(ZoneOffset.UTC)
                .get(timeWindow.getTemporalField());
    }
}
