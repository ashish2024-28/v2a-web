package com.v2s.player.favorites;

import com.v2s.player.data.model.MediaItem;
import com.v2s.player.data.repository.LibraryRepository;

/**
 * FavoritesManager owns favorite actions.
 * Role: exposes readable MediaItem-based methods while keeping persistence inside LibraryRepository.
 */
public final class FavoritesManager {
    private final LibraryRepository repository;

    /** Creates a favorites manager using the shared library repository. */
    public FavoritesManager(LibraryRepository repository) { this.repository = repository; }

    /** Toggles favorite state for a media item and returns its new state. */
    public boolean toggle(MediaItem item) { return repository.toggleFavorite(item.storageKey()); }

    /** Adds a media item to favorites. */
    public void add(MediaItem item) { if (!repository.isFavorite(item.storageKey())) repository.toggleFavorite(item.storageKey()); }

    /** Removes a media item from favorites. */
    public void remove(MediaItem item) { if (repository.isFavorite(item.storageKey())) repository.toggleFavorite(item.storageKey()); }

    /** Returns whether a media item is currently favorite. */
    public boolean isFavorite(MediaItem item) { return repository.isFavorite(item.storageKey()); }
}
