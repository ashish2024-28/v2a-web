package com.v2s.player.search;

import com.v2s.player.data.model.MediaItem;
import com.v2s.player.data.repository.MediaRepository;
import java.util.List;

/**
 * Search feature service.
 * Role: performs case-insensitive title/artist filtering without exposing repository internals to the UI.
 */
public final class SearchEngine {
    private final MediaRepository repository;

    /** Creates a search engine backed by the media repository. */
    public SearchEngine(MediaRepository repository) { this.repository = repository; }

    /** Filters the supplied media list by title or artist. */
    public List<MediaItem> search(List<MediaItem> source, String query) { return repository.search(source, query); }
}
