package com.v2s.player.data.local;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.v2s.player.data.model.MediaItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Android MediaStore data source.
 * Role: performs the only direct queries against the device's local video and audio providers.
 */
public final class MediaStoreDataSource {
    private final ContentResolver resolver;

    /** Creates a MediaStore data source with the application resolver. */
    public MediaStoreDataSource(ContentResolver resolver) {
        this.resolver = resolver;
    }

    /** Reads all local videos ordered by newest added first. */
    public List<MediaItem> loadVideos() {
        List<MediaItem> result = new ArrayList<>();
        String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DURATION};
        try (Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                MediaStore.Video.Media.DATE_ADDED + " DESC")) {
            if (cursor == null) return result;
            int idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
            int durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idIndex);
                Uri uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                result.add(new MediaItem(id, cursor.getString(titleIndex), "", uri, cursor.getLong(durationIndex), true));
            }
        } catch (SecurityException ignored) {
            // Permission state is handled by PermissionManager; an empty result is safer than crashing.
        }
        return result;
    }

    /** Reads music/audio files visible to MediaStore, ordered by newest added first. */
    public List<MediaItem> loadSongs() {
        List<MediaItem> result = new ArrayList<>();
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION};
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        try (Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
                MediaStore.Audio.Media.DATE_ADDED + " DESC")) {
            if (cursor == null) return result;
            int idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idIndex);
                Uri uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                result.add(new MediaItem(id, cursor.getString(titleIndex), cursor.getString(artistIndex), uri,
                        cursor.getLong(durationIndex), false));
            }
        } catch (SecurityException ignored) {
            // Permission state is handled by PermissionManager; an empty result is safer than crashing.
        }
        return result;
    }
}
