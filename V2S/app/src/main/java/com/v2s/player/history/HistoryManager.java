package com.v2s.player.history;

import com.v2s.player.data.model.MediaItem;
import com.v2s.player.data.repository.LibraryRepository;

/**
 * HistoryManager owns recently-played state.
 * Role: keeps HomeActivity independent from persistence details and gives playback code a readable API.
 */
public final class HistoryManager {
    private final LibraryRepository repository;

    /** Creates a history manager backed by the shared library repository. */
    public HistoryManager(LibraryRepository repository) { this.repository = repository; }

    /** Records a media item as recently played. */
    public void add(MediaItem item) { repository.recordHistory(item.storageKey()); }

    /** Records a raw persistence key for compatibility with existing callers. */
    public void record(String mediaKey) { repository.recordHistory(mediaKey); }

    /** Clears all saved history. */
    public void clear() { repository.clearHistory(); }

    /** Returns the number of saved history entries. */
    public int count() { return repository.historyCount(); }
}
