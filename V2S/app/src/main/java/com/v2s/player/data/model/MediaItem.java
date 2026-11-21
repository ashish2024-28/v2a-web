package com.v2s.player.data.model;

import android.net.Uri;
import com.v2s.player.core.utils.FormatUtils;

/**
 * MediaItem is the single immutable model used by the V2S library and player.
 * Role: carries title, artist, URI, duration, type and an optional bundled thumbnail.
 */
public final class MediaItem {
    public final long id;
    public final String title;
    public final String artist;
    public final Uri uri;
    public final long durationMs;
    public final boolean video;
    public final int coverResId;

    /** Creates a media item without a bundled cover image. */
    public MediaItem(long id, String title, String artist, Uri uri, long durationMs, boolean video) {
        this(id, title, artist, uri, durationMs, video, 0);
    }

    /** Creates a media item with an optional local drawable used by the reference-style UI. */
    public MediaItem(long id, String title, String artist, Uri uri, long durationMs, boolean video, int coverResId) {
        this.id = id;
        this.title = title == null || title.trim().isEmpty() ? "Unknown title" : title;
        this.artist = artist == null ? "" : artist;
        this.uri = uri;
        this.durationMs = Math.max(0L, durationMs);
        this.video = video;
        this.coverResId = coverResId;
    }

    /** Returns a collision-safe persistence key that includes the media type. */
    public String storageKey() { return (video ? "v:" : "a:") + id; }

    /** Returns the duration in a compact player-friendly format. */
    public String durationText() { return FormatUtils.duration(durationMs); }
}
