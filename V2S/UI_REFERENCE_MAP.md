# V2S UI Reference Map

## Purpose

This document maps the 23-page V2C visual reference to the implemented V2S feature surfaces. The PDF is an image-only design reference, so the implementation reproduces the visible structure, colors, spacing, controls and navigation patterns as real Android UI rather than embedding screenshots.

## Visual system

- Background: near-black blue (`#050C12`)
- Surfaces: dark blue-gray cards
- Primary accent: V2S orange (`#FF8400`)
- Text: high-contrast white
- Secondary text: muted blue-gray
- Compact rounded cards and pills
- Orange active navigation state
- Media-first cards with artwork, duration and overflow controls

## Page mapping

| PDF pages | Reference area | V2S implementation |
|---|---|---|
| 1-2 | Videos grid/list | `HomeActivity.showVideos()` + `mediaCollection()` |
| 3 | Full video player | `PlayerActivity.buildVideoPlayer()` |
| 4-5 | Combined media/player variants | `HomeActivity` + `PlayerActivity` reusable surfaces |
| 6-7 | Songs list | `HomeActivity.showSongs()` + `songList()` |
| 8 | Audio player / library composition | `PlayerActivity.buildAudioPlayer()` |
| 9-10 | Shorts | `HomeActivity.showShorts()` + `shortCard()` |
| 11-13 | Playlist grid/list | `HomeActivity.showPlaylists()` + `playlistCollection()` |
| 14-16 | Playlist card variants | Shared playlist cards with grid/list toggle |
| 17 | Playlist/library management composition | Playlists + More utility actions |
| 18-19 | Library management variants | Favorites, History, Search, Sort and More |
| 20-22 | More / Settings | `showMore()`, `showSettings()`, `showAbout()` |
| 23 | Complete V2S overview | Combined feature architecture and reusable components |

## Important implementation rule

The reference is visual. Device-specific system bars, actual MediaStore content, permissions and playback duration can differ at runtime. The app therefore reproduces the design language and interaction model without hard-coding a screenshot as the UI.
