package org.example.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeUtil {
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    // Convert Instant (UTC) to IST LocalDateTime
    public static LocalDateTime toIST(Instant utcInstant) {
        if (utcInstant == null) return null;
        return LocalDateTime.ofInstant(utcInstant, IST_ZONE);
    }

    // Convert IST LocalDateTime to Instant (UTC)
    public static Instant toUTC(LocalDateTime istDateTime) {
        if (istDateTime == null) return null;
        return istDateTime.atZone(IST_ZONE).toInstant();
    }

    // Convert Instant (UTC) to UTC LocalDateTime
    public static LocalDateTime toUTCDateTime(Instant utcInstant) {
        if (utcInstant == null) return null;
        return LocalDateTime.ofInstant(utcInstant, UTC_ZONE);
    }

    // Convert LocalDateTime (UTC) to Instant
    public static Instant fromUTCDateTime(LocalDateTime utcDateTime) {
        if (utcDateTime == null) return null;
        return utcDateTime.atZone(UTC_ZONE).toInstant();
    }
} 