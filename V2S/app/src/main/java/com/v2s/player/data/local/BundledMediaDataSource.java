package com.v2s.player.data.local;

import android.content.Context;
import android.net.Uri;
import com.v2s.player.R;
import com.v2s.player.data.model.MediaItem;
import java.util.ArrayList;
import java.util.List;

/**
 * BundledMediaDataSource supplies the built-in demo library.
 * Role: guarantees a useful V2S home screen on a fresh install and provides the artwork used by the PDF-inspired design.
 */
public final class BundledMediaDataSource {
    private final Context context;

    /** Creates a bundled media source using the application context. */
    public BundledMediaDataSource(Context context) { this.context = context.getApplicationContext(); }

    /** Returns the built-in video catalog used when the device library is empty. */
    public List<MediaItem> loadVideos() {
        List<MediaItem> r = new ArrayList<>();
        r.add(video(-2001, "Beautiful Nature 4K Video", "V2S Nature", R.raw.v2s_demo_video_01, R.drawable.thumb_sunset, 324000));
        r.add(video(-2002, "Mountain View Relaxing Video", "V2S Nature", R.raw.v2s_demo_video_02, R.drawable.thumb_lake, 136000));
        r.add(video(-2003, "Waterfall - Sound of Nature", "V2S Nature", R.raw.v2s_demo_video_01, R.drawable.thumb_waterfall, 105000));
        r.add(video(-2004, "City Night Timelapse Video 4K", "V2S City", R.raw.v2s_demo_video_02, R.drawable.thumb_city, 191000));
        r.add(video(-2005, "Surfing Extreme Sports", "V2S Sports", R.raw.v2s_demo_video_01, R.drawable.thumb_surf, 128000));
        r.add(video(-2006, "Super Car Drive Status Video", "V2S Cars", R.raw.v2s_demo_video_02, R.drawable.thumb_car, 275000));
        return r;
    }

    /** Returns the built-in song catalog with artwork matching the reference library structure. */
    public List<MediaItem> loadSongs() {
        List<MediaItem> r = new ArrayList<>();
        r.add(song(-3001, "Shape of You", "Ed Sheeran", R.raw.v2s_demo_song_01, R.drawable.cover_shape, 264000));
        r.add(song(-3002, "I Hate U I Love U", "Gnash", R.raw.v2s_demo_song_02, R.drawable.cover_neon, 227000));
        r.add(song(-3003, "Perfect", "Ed Sheeran", R.raw.v2s_demo_song_03, R.drawable.cover_perfect, 263000));
        r.add(song(-3004, "Kesariya", "Arijit Singh", R.raw.v2s_demo_song_01, R.drawable.cover_kesariya, 276000));
        r.add(song(-3005, "Channa Mereya", "Arijit Singh", R.raw.v2s_demo_song_02, R.drawable.cover_channa, 290000));
        r.add(song(-3006, "Attention", "Charlie Puth", R.raw.v2s_demo_song_03, R.drawable.cover_attention, 211000));
        r.add(song(-3007, "Tum Hi Ho", "Arijit Singh", R.raw.v2s_demo_song_01, R.drawable.cover_tumhi, 261000));
        r.add(song(-3008, "Suno Na Sangemarmar", "Arijit Singh", R.raw.v2s_demo_song_02, R.drawable.cover_suno, 252000));
        return r;
    }

    /** Creates a bundled video model with artwork and a reference-style display duration. */
    private MediaItem video(long id, String title, String artist, int raw, int cover, long duration) {
        return new MediaItem(id, title, artist, resourceUri(raw), duration, true, cover);
    }

    /** Creates a bundled song model with artwork and a reference-style display duration. */
    private MediaItem song(long id, String title, String artist, int raw, int cover, long duration) {
        return new MediaItem(id, title, artist, resourceUri(raw), duration, false, cover);
    }

    /** Builds a stable android.resource URI that Android playback APIs can open. */
    private Uri resourceUri(int resourceId) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + resourceId);
    }
}
