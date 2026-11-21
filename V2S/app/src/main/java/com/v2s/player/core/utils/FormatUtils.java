package com.v2s.player.core.utils;

import java.util.Locale;

/**
 * Formatting utilities.
 * Role: converts raw media values into consistent human-readable text for the UI.
 */
public final class FormatUtils {
    /** Formats a duration as mm:ss or hh:mm:ss. */
    public static String duration(long milliseconds) {
        long totalSeconds = Math.max(0L, milliseconds / 1000L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0) return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private FormatUtils() { }
}
