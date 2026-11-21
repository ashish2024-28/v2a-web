# V2S Architecture

## 1. Architecture goal

The project is divided by responsibility. UI code should not know how MediaStore queries or SharedPreferences serialization work.

```text
┌──────────────────────────────────────────┐
│                 UI LAYER                  │
│ HomeActivity / PlayerActivity             │
└────────────────────┬─────────────────────┘
                     │
┌────────────────────▼─────────────────────┐
│             FEATURE SERVICES              │
│ MediaScanner • MediaSorter • SearchEngine │
│ FavoritesManager • HistoryManager         │
│ PlaylistManager • SettingsManager         │
└────────────────────┬─────────────────────┘
                     │
┌────────────────────▼─────────────────────┐
│              REPOSITORY LAYER             │
│ MediaRepository • LibraryRepository       │
└────────────────────┬─────────────────────┘
                     │
┌────────────────────▼─────────────────────┐
│             DATA SOURCE LAYER             │
│ MediaStoreDataSource                      │
│ PreferencesDataSource                     │
│ PlaylistDataSource                        │
└────────────────────┬─────────────────────┘
                     │
              ┌──────┴──────┐
              │ Android OS  │
              │ MediaStore  │
              │ Preferences │
              └─────────────┘
```

## 2. Folder responsibilities

### `core/`
Shared constants, navigation, permissions, UI components and UI utilities. Nothing here should contain a feature-specific data query.

### `data/model/`
Pure domain objects such as `MediaItem` and `Playlist`.

### `data/local/`
Android storage/provider integrations. This is the only layer that should directly query MediaStore or serialize playlist/library state.

### `data/repository/`
Application-facing data APIs. Repositories combine local data sources and return domain models.

### `media/`
Scanning and sorting operations around the media repository.

### `search/`
Search behavior and filtering.

### `favorites/`
Favorite actions and state access.

### `history/`
Playback history actions.

### `playlists/`
Playlist CRUD and media membership.

### `home/`
Top-level library UI and navigation host.

### `player/`
Playback-only code.

### `settings/`
Persisted user preferences.

## 3. Dependency direction

```text
home/player
     ↓
feature managers/services
     ↓
repositories
     ↓
local data sources
     ↓
Android framework
```

The lower layers do not import Home UI classes.

## 4. Media scanning

```text
User taps Scan Media
        ↓
PermissionManager
        ↓
MediaScanner.scanNow()
        ↓
MediaRepository.refresh()
        ↓
MediaStoreDataSource
        ↓
MediaItem list
        ↓
HomeActivity refresh
```

## 5. Playlist flow

```text
Long press media
        ↓
Add to playlist
        ↓
PlaylistManager
        ↓
PlaylistDataSource
        ↓
SharedPreferences
```

## 6. Playback flow

```text
Tap media
   ↓
HistoryManager.record()
   ↓
AppNavigator.openPlayer()
   ↓
PlayerActivity
   ↓
VideoView / MediaPlayer
```

## 7. Why `MainActivity` is small

`MainActivity` is retained as a launcher compatibility entry point. The actual library implementation is in `home/HomeActivity.java`. This prevents the Android launcher contract from becoming coupled to the entire application UI.

## 8. Future production upgrades

- Replace platform `VideoView`/`MediaPlayer` with Media3 for advanced playback.
- Replace preference-based playlist storage with Room for large libraries.
- Move MediaStore scans off the main thread for very large libraries.
- Add ViewModel/state restoration if the UI becomes more complex.
- Add unit and instrumentation tests for every feature flow.


### Bundled demo media flow

```text
app/src/main/res/raw/
        ↓
BundledMediaDataSource
        ↓
MediaRepository
        ↓
HomeActivity / PlayerActivity
```

Bundled media uses `android.resource://` URIs, so it does not require storage permission and remains available on a fresh install. Device media is loaded separately through `MediaStoreDataSource`.
