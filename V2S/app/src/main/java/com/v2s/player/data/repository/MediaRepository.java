package com.v2s.player.data.repository;

import android.content.Context;
import com.v2s.player.data.local.BundledMediaDataSource;
import com.v2s.player.data.local.MediaStoreDataSource;
import com.v2s.player.data.model.MediaItem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Media repository.
 * Role: exposes a clean API to the UI while hiding direct MediaStore queries and sorting implementation.
 */
public final class MediaRepository {
    private final MediaStoreDataSource source;
    private final BundledMediaDataSource bundledSource;
    private final List<MediaItem> videos = new ArrayList<>();
    private final List<MediaItem> songs = new ArrayList<>();

    /**
     * Creates a media repository backed by device MediaStore plus bundled demo media.
     * The demo files make the app immediately usable on a fresh installation.
     */
    public MediaRepository(Context context) {
        Context appContext = context.getApplicationContext();
        source = new MediaStoreDataSource(appContext.getContentResolver());
        bundledSource = new BundledMediaDataSource(appContext);
    }

    /** Reloads both local videos and songs from the data source. */
    public void refresh() {
        videos.clear();
        videos.addAll(bundledSource.loadVideos());
        videos.addAll(source.loadVideos());
        songs.clear();
        songs.addAll(bundledSource.loadSongs());
        songs.addAll(source.loadSongs());
    }

    /** Returns a defensive copy of the current videos. */
    public List<MediaItem> videos() { return new ArrayList<>(videos); }

    /** Returns a defensive copy of the current songs. */
    public List<MediaItem> songs() { return new ArrayList<>(songs); }

    /** Finds media by title using a case-insensitive contains search. */
    public List<MediaItem> search(List<MediaItem> sourceList, String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) return new ArrayList<>(sourceList);
        List<MediaItem> result = new ArrayList<>();
        for (MediaItem item : sourceList) {
            if (item.title.toLowerCase(Locale.ROOT).contains(normalized) ||
                    item.artist.toLowerCase(Locale.ROOT).contains(normalized)) result.add(item);
        }
        return result;
    }

    /** Sorts a media list by the requested user-visible sort mode. */
    public void sort(List<MediaItem> list, String mode) {
        Comparator<MediaItem> comparator;
        switch (mode) {
            case "name_desc":
                comparator = Comparator.comparing((MediaItem item) -> item.title.toLowerCase(Locale.ROOT), Comparator.reverseOrder());
                break;
            case "duration_asc":
                comparator = Comparator.comparingLong(item -> item.durationMs);
                break;
            case "duration_desc":
                comparator = Comparator.comparingLong((MediaItem item) -> item.durationMs).reversed();
                break;
            default:
                comparator = Comparator.comparing((MediaItem item) -> item.title.toLowerCase(Locale.ROOT));
                break;
        }
        list.sort(comparator);
    }
}
