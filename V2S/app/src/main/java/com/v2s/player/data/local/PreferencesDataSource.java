package com.v2s.player.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.v2s.player.core.constants.AppConstants;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lightweight local preferences data source.
 * Role: persists favorites and recent-history IDs so basic library state survives process restarts.
 */
public final class PreferencesDataSource {
    private final SharedPreferences preferences;

    /** Opens the V2S preference store. */
    public PreferencesDataSource(Context context) {
        preferences = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Returns the persisted favorite MediaStore IDs. */
    public Set<String> readFavorites() {
        return readStringSet(AppConstants.PREF_FAVORITES);
    }

    /** Saves the current favorite MediaStore IDs. */
    public void writeFavorites(Set<String> ids) {
        writeStringSet(AppConstants.PREF_FAVORITES, ids);
    }

    /** Returns persisted history IDs in newest-first order. */
    public List<String> readHistory() {
        String raw = preferences.getString(AppConstants.PREF_HISTORY, "");
        List<String> result = new ArrayList<>();
        for (String part : raw.split(",")) {
            if (part.trim().isEmpty()) continue;
            result.add(part.trim());
        }
        return result;
    }

    /** Saves newest-first history IDs and caps the collection to the application history limit. */
    public void writeHistory(List<String> ids) {
        StringBuilder value = new StringBuilder();
        int count = 0;
        for (String id : ids) {
            if (id == null || count >= AppConstants.MAX_HISTORY_ITEMS || id.trim().isEmpty()) break;
            if (value.length() > 0) value.append(',');
            value.append(id);
            count++;
        }
        preferences.edit().putString(AppConstants.PREF_HISTORY, value.toString()).apply();
    }

    /** Clears all persisted history IDs. */
    public void clearHistory() {
        preferences.edit().remove(AppConstants.PREF_HISTORY).apply();
    }

    /** Reads a comma-separated set of long IDs from preferences. */
    private Set<String> readStringSet(String key) {
        String raw = preferences.getString(key, "");
        Set<String> result = new LinkedHashSet<>();
        for (String part : raw.split(",")) {
            if (part.trim().isEmpty()) continue;
            result.add(part.trim());
        }
        return result;
    }

    /** Writes a set of long IDs in a deterministic comma-separated representation. */
    private void writeStringSet(String key, Set<String> ids) {
        StringBuilder value = new StringBuilder();
        for (String id : ids) {
            if (id == null || id.trim().isEmpty()) continue;
            if (value.length() > 0) value.append(',');
            value.append(id);
        }
        preferences.edit().putString(key, value.toString()).apply();
    }
}
