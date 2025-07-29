package org.example.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class for handling date/time conversions.
 * 
 * Date handling strategy:
 * - Frontend sends dates in IST (Asia/Kolkata timezone) as ZonedDateTime
 * - Database stores dates in IST (Asia/Kolkata timezone) as ZonedDateTime
 * - All dates maintain timezone information throughout the application
 * 
 * This ensures consistent timezone handling across the application.
 */
public class TimeUtil {
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    /**
     * Convert any ZonedDateTime to IST ZonedDateTime
     * @param dateTime ZonedDateTime to convert
     * @return IST ZonedDateTime
     */
    public static ZonedDateTime toIST(ZonedDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.withZoneSameInstant(IST_ZONE);
    }

    /**
     * Convert any ZonedDateTime to UTC ZonedDateTime
     * @param dateTime ZonedDateTime to convert
     * @return UTC ZonedDateTime
     */
    public static ZonedDateTime toUTC(ZonedDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.withZoneSameInstant(UTC_ZONE);
    }

    /**
     * Get current time in IST
     * @return Current ZonedDateTime in IST
     */
    public static ZonedDateTime nowIST() {
        return ZonedDateTime.now(IST_ZONE);
    }

    /**
     * Get current time in UTC
     * @return Current ZonedDateTime in UTC
     */
    public static ZonedDateTime nowUTC() {
        return ZonedDateTime.now(UTC_ZONE);
    }

    public static double round2(double value) {
        return new java.math.BigDecimal(value).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }
} 