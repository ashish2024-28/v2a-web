# V2S QA Checklist

## Build

- [ ] Android Studio opens the project without Gradle sync errors.
- [ ] JDK 17 is selected.
- [ ] Android SDK 36 is installed.
- [ ] Debug APK builds successfully.
- [ ] Release APK builds with R8/shrinking enabled.

## Permissions

- [ ] Fresh install asks for media permissions when Scan Media is tapped.
- [ ] Granting permission scans videos and songs.
- [ ] Denying permission does not crash the application.
- [ ] Scan can be retried after permission is granted from system settings.

## Videos

- [ ] Videos appear after scanning.
- [ ] Grid view works.
- [ ] List view works.
- [ ] Search filters title.
- [ ] Sort A–Z works.
- [ ] Sort Z–A works.
- [ ] Duration sorting works.
- [ ] Tapping a video opens playback.
- [ ] Back from player returns to the library.

## Songs

- [ ] Songs appear after scanning.
- [ ] Song search works by title and artist.
- [ ] Song sorting works.
- [ ] Tapping a song starts audio playback.

## Favorites

- [ ] Long press opens media actions.
- [ ] Add favorite works.
- [ ] Remove favorite works.
- [ ] Favorites survive app restart.
- [ ] Favorite count updates.

## History

- [ ] Playing media creates a history entry.
- [ ] History is newest first.
- [ ] History survives app restart.
- [ ] Clear History removes entries.

## Playlists

- [ ] Create playlist works.
- [ ] Empty playlist state works.
- [ ] Long press → Add to playlist works.
- [ ] Playlist detail resolves current media.
- [ ] Delete playlist works.
- [ ] Playlist data survives app restart.
- [ ] Playlist grid/list toggle works.

## UI/UX

- [ ] Long titles do not overflow cards.
- [ ] Long pages scroll correctly.
- [ ] Empty states are readable.
- [ ] Controls have content descriptions where appropriate.
- [ ] Dark background remains consistent across screens.
- [ ] No feature is presented as implemented when it is only planned.

## Device matrix

- [ ] Android 8–12.
- [ ] Android 13.
- [ ] Android 14.
- [ ] Android 15.
- [ ] Android 16.
- [ ] Small phone.
- [ ] Large phone.
- [ ] Tablet.
