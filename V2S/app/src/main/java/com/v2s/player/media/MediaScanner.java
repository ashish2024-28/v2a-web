package com.v2s.player.media;

import com.v2s.player.data.repository.MediaRepository;

/**
 * Media scanning coordinator.
 * Role: gives the UI one named operation for refreshing the complete local media library.
 */
public final class MediaScanner {
    private final MediaRepository repository;

    /** Creates a scanner around the shared media repository. */
    public MediaScanner(MediaRepository repository) { this.repository = repository; }

    /** Refreshes videos and songs from MediaStore. */
    public void scanNow() { repository.refresh(); }
}
