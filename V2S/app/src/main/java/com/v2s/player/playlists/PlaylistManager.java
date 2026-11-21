package com.v2s.player.playlists;

import android.content.Context;
import com.v2s.player.data.local.PlaylistDataSource;
import com.v2s.player.data.model.Playlist;
import java.util.List;

/**
 * Playlist feature manager.
 * Role: provides named create/delete/add/remove operations without leaking SharedPreferences into screens.
 */
public final class PlaylistManager {
    private final PlaylistDataSource source;

    /** Creates a playlist manager using local persistence. */
    public PlaylistManager(Context context) { source = new PlaylistDataSource(context); }

    /** Returns all saved playlists. */
    public List<Playlist> all() { return source.loadAll(); }

    /** Creates a new playlist from user input. */
    public void create(String name) { source.create(name); }

    /** Deletes the named playlist. */
    public void delete(String name) { source.delete(name); }

    /** Adds a media item ID to the named playlist. */
    public void add(String name, String mediaKey) { source.add(name, mediaKey); }

    /** Removes a media item ID from the named playlist. */
    public void remove(String name, String mediaKey) { source.remove(name, mediaKey); }
}
