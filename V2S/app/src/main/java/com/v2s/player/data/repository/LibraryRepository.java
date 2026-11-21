package com.v2s.player.data.repository;

import android.content.Context;
import com.v2s.player.data.local.PreferencesDataSource;
import com.v2s.player.data.model.MediaItem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User library state repository.
 * Role: persists favorites/history and resolves persisted IDs back to the current MediaStore objects.
 */
public final class LibraryRepository {
    private final PreferencesDataSource source;
    private final Set<String> favorites;
    private final List<String> historyIds;

    /** Opens the persisted library state for the current application. */
    public LibraryRepository(Context context) {
        source = new PreferencesDataSource(context);
        favorites = new HashSet<>(source.readFavorites());
        historyIds = new ArrayList<>(source.readHistory());
    }

    /** Returns whether the media ID is marked as favorite. */
    public boolean isFavorite(String mediaKey) { return favorites.contains(mediaKey); }

    /** Toggles favorite state and persists the result immediately. */
    public boolean toggleFavorite(String mediaKey) {
        if (!favorites.remove(mediaKey)) favorites.add(mediaKey);
        source.writeFavorites(favorites);
        return favorites.contains(mediaKey);
    }

    /** Adds a media ID to newest-first history and persists it. */
    public void recordHistory(String mediaKey) {
        historyIds.remove(mediaKey);
        historyIds.add(0, mediaKey);
        while (historyIds.size() > com.v2s.player.core.constants.AppConstants.MAX_HISTORY_ITEMS) {
            historyIds.remove(historyIds.size() - 1);
        }
        source.writeHistory(historyIds);
    }

    /** Clears persisted playback history. */
    public void clearHistory() {
        historyIds.clear();
        source.clearHistory();
    }

    /** Resolves favorite IDs against the current video and song collections. */
    public List<MediaItem> resolveFavorites(List<MediaItem> videos, List<MediaItem> songs) {
        return resolveKeys(new ArrayList<>(favorites), videos, songs);
    }

    /** Resolves history IDs against the current video and song collections. */
    public List<MediaItem> resolveHistory(List<MediaItem> videos, List<MediaItem> songs) {
        return resolveKeys(historyIds, videos, songs);
    }

    /** Returns a defensive copy of all favorite IDs. */
    public Set<String> favoriteIds() { return new HashSet<>(favorites); }

    /** Returns the number of saved history IDs. */
    public int historyCount() { return historyIds.size(); }

    /** Finds media models matching persisted IDs while preserving requested ID order. */
    private List<MediaItem> resolveKeys(List<String> keys, List<MediaItem> videos, List<MediaItem> songs) {
        List<MediaItem> result = new ArrayList<>();
        for (String key : keys) {
            MediaItem found = find(key, videos, songs);
            if (found != null) result.add(found);
        }
        return result;
    }

    /** Looks up one media item by its MediaStore ID. */
    private MediaItem find(String key, List<MediaItem> videos, List<MediaItem> songs) {
        for (MediaItem item : videos) if (item.storageKey().equals(key)) return item;
        for (MediaItem item : songs) if (item.storageKey().equals(key)) return item;
        return null;
    }
}
