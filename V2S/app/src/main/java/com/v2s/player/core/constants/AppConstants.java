package com.v2s.player.core.constants;

/**
 * Application-wide constants.
 * Role: keeps permission request IDs, preference names and shared limits in one readable place.
 */
public final class AppConstants {
    public static final int REQUEST_MEDIA_PERMISSIONS = 100;
    public static final int MAX_HISTORY_ITEMS = 50;
    public static final String PREFS_NAME = "v2s_preferences";
    public static final String PREF_FAVORITES = "favorite_media_ids";
    public static final String PREF_HISTORY = "history_media_ids";

    private AppConstants() { }
}
