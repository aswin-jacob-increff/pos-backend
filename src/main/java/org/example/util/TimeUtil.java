package org.example.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Utility class for handling date/time conversions between IST and UTC.
 * 
 * Date handling strategy:
 * - Frontend sends dates in IST (Asia/Kolkata timezone)
 * - Database stores dates in UTC (Instant)
 * - When sending data to frontend, convert UTC back to IST
 * 
 * This ensures consistent timezone handling across the application.
 */
public class TimeUtil {
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    /**
     * Convert UTC Instant to IST LocalDateTime for frontend display
     * @param utcInstant UTC timestamp from database
     * @return IST LocalDateTime for frontend
     */
    public static LocalDateTime toIST(Instant utcInstant) {
        if (utcInstant == null) return null;
        return LocalDateTime.ofInstant(utcInstant, IST_ZONE);
    }

    /**
     * Convert IST LocalDateTime from frontend to UTC Instant for database storage
     * @param istDateTime IST timestamp from frontend
     * @return UTC Instant for database
     */
    public static Instant toUTC(LocalDateTime istDateTime) {
        if (istDateTime == null) return null;
        return istDateTime.atZone(IST_ZONE).toInstant();
    }

    /**
     * Convert UTC Instant to UTC LocalDateTime (utility method)
     * @param utcInstant UTC timestamp
     * @return UTC LocalDateTime
     */
    public static LocalDateTime toUTCDateTime(Instant utcInstant) {
        if (utcInstant == null) return null;
        return LocalDateTime.ofInstant(utcInstant, UTC_ZONE);
    }

    /**
     * Convert UTC LocalDateTime to UTC Instant (utility method)
     * @param utcDateTime UTC LocalDateTime
     * @return UTC Instant
     */
    public static Instant fromUTCDateTime(LocalDateTime utcDateTime) {
        if (utcDateTime == null) return null;
        return utcDateTime.atZone(UTC_ZONE).toInstant();
    }
} 