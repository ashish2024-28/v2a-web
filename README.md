# V2S — Video, Shorts & Songs

V2S is a local-first Android media player built with Kotlin, Jetpack Compose, Material 3, Room, MediaStore and AndroidX Media3/ExoPlayer.

This release is based on the supplied **V4 + V5** project sources. Existing screens, navigation, player controls, playlists, folders, favorites, history, Shorts, lyrics, mini-player, PiP and notification UI were kept. The work here is focused on correcting playback/data bugs, stabilizing the player pipeline, updating Media3 and removing generated/unused project scaffolding.

## Main fixes in this release

### Video and song playback
- MediaStore video MIME type is now retained with every `VideoItem`.
- MediaStore audio MIME type is now retained with every `SongItem`.
- `content://media/.../<numeric-id>` URIs no longer depend on a file extension being present in the URI.
- Playback resolves media type from the most reliable available source: MediaStore MIME type, file extension, or Media3 extractor sniffing.
- `application/octet-stream` and wildcard MIME values no longer hide a useful file extension.
- Video-as-audio playback keeps the original container MIME type.
- Resume-position logic no longer accidentally forces every video to position 0 when the caller supplies the default start position.
- `setMediaItem(item, startPositionMs)` is used so the start position is part of the player operation before preparation.
- Existing decoder fallback/recovery behavior is retained.

### Player stability
- Media3 updated from 1.5.1 to stable 1.11.0.
- Hardware decoding remains the normal/default path.
- Existing software-decoder fallback remains available.
- Existing resource/decoder recovery logic remains enabled.
- Existing audio focus, volume, speed, repeat, shuffle, subtitles, tracks, AB repeat, sleep timer and progress persistence remain in the player manager.

### Media scanning
- Audio scanning now reads `MediaStore.Audio.Media.MIME_TYPE`.
- Non-standard formats discovered through `MediaStore.Files` receive a useful MIME hint when Android does not provide one.
- Existing system-sound filtering remains.
- Existing multi-volume MediaStore scanning remains.

### Cleanup
Removed generated or development-only scaffolding that was not part of the application runtime:
- patch/fix shell scripts
- temporary Python patch scripts
- temporary `test.kt`
- generated project metadata
- AI Studio asset scaffolding
- unused applet documentation/module folder
- duplicate project markdown files
- generic example unit test
- generated screenshot artifact

There is now **one project documentation file: `README.md`**.

## Existing UI and functionality preserved

The cleanup does not intentionally remove application features. The project still contains the existing UI and logic for:

- Home
- Videos / local library
- Songs / audio player
- Folders
- Playlists
- Favorites
- History / continue watching
- Search
- Sort and filters
- Shorts vertical feed
- Normal full-screen player
- Persistent mini-player
- Picture-in-picture
- Lyrics
- Subtitles
- Audio/video track selection
- Playback speed
- Repeat and shuffle
- Volume and brightness controls
- Equalizer controls
- A-B repeat
- Sleep timer
- Play video as audio
- Background notification controls
- Headset/earphone controls
- Sharing and media information
- Delete/favorite/history operations

## Playback architecture

```text
MediaStore / device media
          |
          v
   VideoRepository
          |
          +---- VideoItem / SongItem
          |
          v
   MainViewModel
          |
          v
   VyloPlayerManager
          |
          v
   Media3 ExoPlayer
          |
          +---- DefaultMediaSourceFactory
          +---- DefaultExtractorsFactory
          +---- MediaCodec decoders
          |
          v
   PlayerView / Mini Player / Shorts
```

The normal playback path is one `ExoPlayer` instance. The existing multiple-playback setting remains opt-in.

## Media format expectations

Media3 1.11.0 supports common progressive containers including MP4, M4A, WebM, Matroska, MP3, Ogg, WAV, MPEG-TS, MPEG-PS, FLV, ADTS/AAC, FLAC and AMR. The actual audio/video codec must also be supported by the Android device. H.264, HEVC, VP8/VP9 and AV1 availability depends on Android version and device decoder support.

V2S provides MIME/container hints where they are useful, but a MIME hint cannot make an unsupported codec magically playable.

## Test cases

The source tree includes automated tests for:

1. MediaStore MIME type wins over a numeric `content://` URI.
2. MP3 extension resolves correctly when the URI has no extension.
3. MKV and WebM receive explicit container hints.
4. Wildcard MIME values do not override a known extension.
5. Generic `application/octet-stream` does not hide a known extension.
6. Unknown media remains available for extractor sniffing.
7. Room playlist creation, rename, item insertion/removal and deletion.
8. Video metadata favorites/history/progress persistence.
9. Audio metadata persistence.
10. LRC parsing with normal and multiple timestamps.
11. Lyrics active-line calculation.
12. Video sorting/filtering.
13. Shorts/feed uniqueness behavior.
14. Existing Compose/Robolectric home UI smoke coverage.

### Manual device test matrix

Before release, test on a physical Android device with:

| Test | Expected result |
|---|---|
| MP4 H.264 + AAC | Video and audio play |
| MP4 HEVC | Plays when device decoder supports HEVC |
| MKV H.264/HEVC | Plays when contained codecs are supported |
| WebM VP8/VP9 | Plays when device supports the codec |
| MP3 | Audio plays and progress updates |
| M4A/AAC | Audio plays |
| FLAC | Audio plays when decoder is available |
| OGG/Opus | Audio plays when decoder is available |
| WAV | Audio plays |
| TS/MTS/M2TS | Plays when contained streams are supported |
| 3GP | Plays when contained streams are supported |
| Video as audio | Audio continues without requiring video UI |
| Resume | Saved position is restored when appropriate |
| Seek | Position changes correctly |
| Pause/play | State and notification update correctly |
| Next/previous | Correct queue item opens |
| Background audio | Playback continues according to the background-play setting |
| PiP | Same playback session continues |
| Shorts | Active video is rendered and changes with the feed |
| Subtitle sidecar | Subtitle appears when supported |
| External subtitle | Selected subtitle is attached to current media |
| Decoder recovery | Decoder failure surfaces an error or recovery attempt instead of a blank/crash state |

## Build

Open the project in Android Studio and allow Gradle to sync.

The project uses:

- compileSdk 36
- minSdk 24
- targetSdk 36
- Java 17
- Kotlin 2.2.10
- Android Gradle Plugin 9.1.1
- Media3 1.11.0

Typical commands after Android Studio/Gradle has resolved the project:

```text
./gradlew test
./gradlew assembleDebug
```

Windows:

```text
gradlew.bat test
gradlew.bat assembleDebug
```

The supplied archives did not contain a Gradle wrapper or Android SDK, so this delivery was checked through source/configuration analysis and targeted static validation rather than claiming a local APK build that was not actually executed.

## Important playback limitation

No Android player can guarantee every codec/container on every phone. Android's platform decoder capabilities vary by device. For unsupported media, V2S should show the player error/recovery state rather than silently displaying a blank player.

## Project structure

```text
V2S/
├── app/
│   ├── src/main/java/com/example/
│   │   ├── data/
│   │   ├── domain/
│   │   ├── player/
│   │   └── ui/
│   ├── src/main/res/
│   └── src/test/
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── README.md
```

## Source verification notes

The supplied V4 and V5 versions were compared before applying changes. V5 already contained the later player recovery and lifecycle changes, so those were retained rather than reverting to V4. The main functional correction was to make the MediaStore-to-Media3 media-type handoff explicit and reliable while preserving the existing UI/player architecture.
