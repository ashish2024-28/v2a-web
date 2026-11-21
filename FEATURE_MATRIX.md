# V2S Feature Matrix

| Feature | Implementation | Persistence | Main entry |
|---|---|---|---|
| Video scan | Yes | MediaStore | Videos → Scan Media |
| Audio scan | Yes | MediaStore | Songs → Scan |
| Video playback | Yes | None | Tap video |
| Audio playback | Yes | None | Tap song |
| Grid/list toggle | Yes | In-memory UI state | Videos/Songs/Playlists |
| Search | Yes | In-memory query | Header search |
| Sort | Yes | In-memory sort | ⇅ button |
| Favorites | Yes | SharedPreferences | More → Favorites |
| History | Yes | SharedPreferences | More → History |
| Clear history | Yes | SharedPreferences | History → Clear History |
| Create playlist | Yes | SharedPreferences | Playlists → + |
| Add media to playlist | Yes | SharedPreferences | Long press → Add to playlist |
| Delete playlist | Yes | SharedPreferences | Long press/delete |
| Shorts feed | Yes | MediaStore videos | Shorts |
| Settings | Yes | SharedPreferences | More → Settings |
| About | Yes | None | More → About |
| Android runtime permissions | Yes | Android system | Scan Media |
| Advanced network playback | No | — | Planned Media3 upgrade |
| Casting | No | — | Planned |
| Screen recording | No | — | Planned |
