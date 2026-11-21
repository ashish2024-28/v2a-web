# V2S — Video & Audio Player

V2S is a local-first Android video and audio player inspired by the supplied V2C visual reference.

## Included

- PDF-inspired Videos grid/list UI
- Songs library with album artwork
- Shorts feed
- Playlists with grid/list mode
- Favorites
- History
- Search
- Sort
- MediaStore scanning
- Bundled demo videos and royalty-free generated demo tones
- Full-screen video player
- Audio player with progress
- Settings and About
- Dark V2S design system
- Clear feature-based Java architecture
- File and method documentation

## Architecture

```text
core/ui       Shared visual components
core/utils    Small utilities
home          Main library shell
media         Media scanning/sorting
player        Video/audio playback
playlists     Playlist feature
favorites     Favorite state
history       Playback history
search        Search feature
settings      Preferences
data           Models, local data sources and repositories
```

## Reference matching

See `UI_REFERENCE_MAP.md` for the 23-page mapping and `DESIGN_SYSTEM.md` for the visual tokens.

## Build

Open the project in Android Studio and sync Gradle. The project uses Java 17, compile SDK 36 and Android Gradle Plugin 9.1.1.

Because this delivery environment does not contain an Android SDK/Gradle executable, an APK was not falsely claimed as compiled here. Android Studio should perform the real Gradle build on a configured Android development machine.
