package com.v2s.player.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Settings manager.
 * Role: persists small user preferences without making individual screens responsible for storage details.
 */
public final class SettingsManager {
    private static final String PREFS = "v2s_settings";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_BACKGROUND_PLAYBACK = "background_playback";
    private final SharedPreferences preferences;

    /** Opens the V2S settings preference store. */
    public SettingsManager(Context context) { preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    /** Returns the saved dark-mode preference. */
    public boolean isDarkMode() { return preferences.getBoolean(KEY_DARK_MODE, true); }

    /** Saves the dark-mode preference. */
    public void setDarkMode(boolean enabled) { preferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply(); }

    /** Returns whether the user enabled background playback in settings. */
    public boolean isBackgroundPlaybackEnabled() { return preferences.getBoolean(KEY_BACKGROUND_PLAYBACK, false); }

    /** Saves the background-playback preference for future player integration. */
    public void setBackgroundPlaybackEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_BACKGROUND_PLAYBACK, enabled).apply();
    }
}
