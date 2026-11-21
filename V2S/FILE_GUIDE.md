# V2S File Guide

Every filename should describe its responsibility. Avoid generic names such as `Helper`, `Utils2`, `Data`, `Manager2`, or `NewActivity`.

| File | Role |
|---|---|
| `V2SApplication.java` | Process-level application entry point. |
| `MainActivity.java` | Stable launcher bridge to HomeActivity. |
| `home/HomeActivity.java` | Top-level UI, navigation and library screens. |
| `player/PlayerActivity.java` | Local video/audio playback screen. |
| `core/constants/AppConstants.java` | Shared application constants. |
| `core/constants/MediaConstants.java` | Media sorting/constants. |
| `core/navigation/AppNavigator.java` | Central Activity navigation. |
| `core/permissions/PermissionManager.java` | Runtime media permission handling. |
| `core/utils/FormatUtils.java` | Duration/text formatting. |
| `core/utils/ViewUtils.java` | dp conversion and reusable UI backgrounds. |
| `data/model/MediaItem.java` | Immutable video/song model. |
| `data/model/MediaType.java` | Explicit VIDEO/AUDIO type enum. |
| `data/model/Playlist.java` | Playlist domain model. |
| `data/local/MediaStoreDataSource.java` | Direct MediaStore queries. |
| `data/local/PreferencesDataSource.java` | Favorites/history persistence. |
| `data/local/PlaylistDataSource.java` | Playlist persistence. |
| `data/repository/MediaRepository.java` | Media query/search/sort API. |
| `data/repository/LibraryRepository.java` | Favorites/history state API. |
| `media/MediaScanner.java` | Media refresh coordinator. |
| `media/MediaSorter.java` | Sorting facade. |
| `search/SearchEngine.java` | Title/artist search. |
| `favorites/FavoritesManager.java` | Favorite actions. |
| `history/HistoryManager.java` | Playback history actions. |
| `playlists/PlaylistManager.java` | Playlist CRUD and membership. |
| `settings/SettingsManager.java` | User preference persistence. |
| `res/drawable/ic_v2s.xml` | V2S vector mark. |
| `res/values/colors.xml` | V2S color system. |
| `res/values/strings.xml` | App/localization strings. |
| `res/values/themes.xml` | Application theme. |
| `AndroidManifest.xml` | Permissions, app identity and Activity declarations. |
| `app/build.gradle` | Android module build configuration. |
| `proguard-rules.pro` | Release shrinker rules. |

## New PDF-matching UI files

- `core/ui/V2SUi.java` — shared visual system: colors, surfaces, buttons, thumbnails, spacing.
- `home/HomeActivity.java` — all library-facing screens and navigation shell.
- `player/PlayerActivity.java` — custom video/audio playback surface.
- `UI_REFERENCE_MAP.md` — mapping between the 23 reference pages and implementation methods.
- `DESIGN_SYSTEM.md` — exact UI tokens and layout rules used by the implementation.
