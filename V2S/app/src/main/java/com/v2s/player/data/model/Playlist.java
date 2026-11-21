package com.v2s.player.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Playlist domain model.
 * Role: represents a named collection of MediaStore IDs without tying the model to Android UI classes.
 */
public final class Playlist {
    public final String name;
    private final List<String> mediaIds;

    /** Creates a playlist with a defensive copy of its media IDs. */
    public Playlist(String name, List<String> mediaIds) {
        this.name = name;
        this.mediaIds = new ArrayList<>(mediaIds);
    }

    /** Returns an immutable view of this playlist's media IDs. */
    public List<String> mediaIds() {
        return Collections.unmodifiableList(mediaIds);
    }
}
