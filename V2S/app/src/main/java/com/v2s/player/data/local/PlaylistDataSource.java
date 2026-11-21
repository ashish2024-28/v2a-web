package com.v2s.player.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.v2s.player.data.model.Playlist;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Playlist persistence data source.
 * Role: stores playlist names and MediaStore IDs using Android SharedPreferences for this dependency-free build.
 */
public final class PlaylistDataSource {
    private static final String PREF_NAME = "v2s_playlists";
    private static final String NAMES_KEY = "names";
    private final SharedPreferences preferences;

    /** Opens the local playlist preference store. */
    public PlaylistDataSource(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Returns all playlists in stable creation order. */
    public List<Playlist> loadAll() {
        List<Playlist> result = new ArrayList<>();
        for (String name : names()) result.add(new Playlist(name, loadIds(name)));
        return result;
    }

    /** Creates a playlist if the supplied name is non-empty and not already present. */
    public void create(String name) {
        String clean = normalizeName(name);
        if (clean.isEmpty()) return;
        Set<String> updated = names();
        updated.add(clean);
        preferences.edit().putStringSet(NAMES_KEY, updated).apply();
    }

    /** Deletes a playlist and its associated item list. */
    public void delete(String name) {
        Set<String> updated = names();
        updated.remove(name);
        preferences.edit().putStringSet(NAMES_KEY, updated).remove(key(name)).apply();
    }

    /** Adds a media ID to a playlist when it is not already present. */
    public void add(String name, String mediaKey) {
        List<String> ids = loadIds(name);
        if (!ids.contains(mediaKey)) ids.add(mediaKey);
        saveIds(name, ids);
    }

    /** Removes a media ID from a playlist. */
    public void remove(String name, String mediaKey) {
        List<String> ids = loadIds(name);
        ids.remove(mediaKey);
        saveIds(name, ids);
    }

    /** Returns playlist names as a defensive copy. */
    private Set<String> names() {
        return new LinkedHashSet<>(preferences.getStringSet(NAMES_KEY, new LinkedHashSet<>()));
    }

    /** Reads media IDs stored for one playlist. */
    private List<String> loadIds(String name) {
        String raw = preferences.getString(key(name), "");
        List<String> ids = new ArrayList<>();
        for (String part : raw.split(",")) {
            if (part.trim().isEmpty()) continue;
            ids.add(part.trim());
        }
        return ids;
    }

    /** Saves media IDs for one playlist. */
    private void saveIds(String name, List<String> ids) {
        StringBuilder value = new StringBuilder();
        for (String id : ids) {
            if (value.length() > 0) value.append(',');
            value.append(id);
        }
        preferences.edit().putString(key(name), value.toString()).apply();
    }

    /** Creates a collision-resistant preference key for a playlist name. */
    private String key(String name) {
        return "playlist_" + Integer.toHexString(name == null ? 0 : name.hashCode());
    }

    /** Normalizes user-entered playlist names. */
    private String normalizeName(String name) {
        return name == null ? "" : name.trim();
    }
}
