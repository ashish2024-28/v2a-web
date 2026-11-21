package com.v2s.player.media;

import com.v2s.player.data.model.MediaItem;
import com.v2s.player.data.repository.MediaRepository;
import java.util.List;

/**
 * Media sorting facade.
 * Role: keeps sort commands readable in screen code and centralizes the accepted sort modes.
 */
public final class MediaSorter {
    private final MediaRepository repository;

    /** Creates a sorter around the shared media repository. */
    public MediaSorter(MediaRepository repository) { this.repository = repository; }

    /** Sorts a media list using a named V2S sort mode. */
    public void sort(List<MediaItem> items, String mode) { repository.sort(items, mode); }
}
