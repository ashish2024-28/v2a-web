# V2S Bundled Sample Media

V2S includes small, self-generated demo media so a fresh installation has content even when the phone has no videos or songs.

## Videos

- `v2s_demo_video_01.mp4` — V2S Demo Video 01 — 6 seconds
- `v2s_demo_video_02.mp4` — V2S Demo Video 02 — 8 seconds

## Songs

- `v2s_demo_song_01.mp3` — V2S Demo Beat 01 — 8 seconds
- `v2s_demo_song_02.mp3` — V2S Demo Beat 02 — 8 seconds
- `v2s_demo_song_03.mp3` — V2S Demo Beat 03 — 8 seconds

## Implementation

The files live in `app/src/main/res/raw/` and are exposed by `BundledMediaDataSource`. They are combined with device MediaStore results by `MediaRepository`.

These are generated demo tones/video patterns, not commercial music or third-party copyrighted media.
