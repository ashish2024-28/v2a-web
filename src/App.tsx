import React, { useState, useRef, useEffect, useCallback } from 'react';
import { MediaItem, MediaType, Playlist } from './types/media';
import { Navbar } from './components/Navbar';
import { LibraryView } from './components/LibraryView';
import { VideoPlayerModal } from './components/VideoPlayerModal';
import { ShortsPlayerView } from './components/ShortsPlayerView';
import { AudioPlayerDeck } from './components/AudioPlayerDeck';
import { EqualizerModal } from './components/EqualizerModal';
import { ApkHubModal } from './components/ApkHubModal';
import { NetworkStreamModal } from './components/NetworkStreamModal';
import { FileImportModal } from './components/FileImportModal';
import {
  describeMediaPlaybackError,
  extractPlayableMedia,
  extractPlaylistFromUrl,
  fetchOEmbedMetadata,
  isPersistableMediaItem,
  isPlaylistUrl,
  parseMediaUrl,
  validateMediaUrl,
} from './utils/mediaParser';
import {
  Link2, FolderOpen, Film, Flame, Music, Plus, Sliders,
  AlertCircle, CheckCircle2, X, Play, RotateCcw
} from 'lucide-react';

const MEDIA_LINKS_STORAGE_KEY = 'v2s_user_media_links_v1';
const PLAYLISTS_STORAGE_KEY = 'v2s_user_playlists_v1';
const LAST_PLAYBACK_STORAGE_KEY = 'v2s_last_playback_v1';

interface LastPlaybackState {
  itemId: string;
  currentTime: number;
  updatedAt: number;
}

function loadSavedMediaLinks(): MediaItem[] {
  try {
    const raw = localStorage.getItem(MEDIA_LINKS_STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function loadSavedPlaylists(): Playlist[] {
  try {
    const raw = localStorage.getItem(PLAYLISTS_STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function loadLastPlayback(): LastPlaybackState | null {
  try {
    const raw = localStorage.getItem(LAST_PLAYBACK_STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    if (parsed && typeof parsed.itemId === 'string') {
      return parsed as LastPlaybackState;
    }
    return null;
  } catch {
    return null;
  }
}

export default function App() {
  const [mediaItems, setMediaItems] = useState<MediaItem[]>(loadSavedMediaLinks);
  const [playlists, setPlaylists] = useState<Playlist[]>(loadSavedPlaylists);
  const [activeTab, setActiveTab] = useState<string>('all');
  const [selectedShortId, setSelectedShortId] = useState<string | null>(null);
  const [activePlaylistId, setActivePlaylistId] = useState<string | null>(null);

  // Resume bar state when reopening app after tab/app close
  const [resumeBanner, setResumeBanner] = useState<{ item: MediaItem; currentTime: number } | null>(null);
  const [videoResumeSec, setVideoResumeSec] = useState<number>(0);

  // Inline quick add link bar state
  const [quickUrl, setQuickUrl] = useState('');
  const [quickTitle, setQuickTitle] = useState('');
  const [quickType, setQuickType] = useState<MediaType>('video');
  const [quickSaving, setQuickSaving] = useState(false);
  const [quickError, setQuickError] = useState<string | null>(null);
  const [quickUnverifiedPrompt, setQuickUnverifiedPrompt] = useState(false);

  // App-wide smooth feedback toast
  const [appNotice, setAppNotice] = useState<{ text: string; tone: 'info' | 'error' } | null>(null);
  const noticeTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const showNotice = useCallback((text: string, tone: 'info' | 'error' = 'info') => {
    setAppNotice({ text, tone });
    if (noticeTimerRef.current) clearTimeout(noticeTimerRef.current);
    noticeTimerRef.current = setTimeout(() => setAppNotice(null), 3200);
  }, []);

  // Video Player state
  const [activeVideo, setActiveVideo] = useState<MediaItem | null>(null);

  // Audio / Background Player state
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const [currentSong, setCurrentSong] = useState<MediaItem | null>(null);
  const [isPlayingAudio, setIsPlayingAudio] = useState(false);
  const [audioError, setAudioError] = useState<string | null>(null);
  const lastSavedTickRef = useRef<number>(0);

  // Equalizer state
  const [eqGains, setEqGains] = useState<number[]>([0, 0, 0, 0, 0, 0, 0, 0, 0, 0]);
  const [bassBoost, setBassBoost] = useState(25);
  const [virtualizer, setVirtualizer] = useState(15);
  const [eqEnabled, setEqEnabled] = useState(true);

  // Modals
  const [showEqModal, setShowEqModal] = useState(false);
  const [showApkModal, setShowApkModal] = useState(false);
  const [showStreamModal, setShowStreamModal] = useState(false);
  const [editingItem, setEditingItem] = useState<MediaItem | null>(null);
  const [showImportModal, setShowImportModal] = useState(false);

  // Check if a persisted session exists on initial load
  useEffect(() => {
    const saved = loadLastPlayback();
    if (!saved) return;
    const found = mediaItems.find((m) => m.id === saved.itemId);
    if (found) {
      setResumeBanner({ item: found, currentTime: saved.currentTime || 0 });
    }
  }, []);

  // Persist user-added links to localStorage (device blob URLs stay in-session)
  useEffect(() => {
    try {
      const persistable = mediaItems.filter(isPersistableMediaItem);
      localStorage.setItem(MEDIA_LINKS_STORAGE_KEY, JSON.stringify(persistable));
    } catch {
      // ignore storage quota errors
    }
  }, [mediaItems]);

  useEffect(() => {
    try {
      localStorage.setItem(PLAYLISTS_STORAGE_KEY, JSON.stringify(playlists));
    } catch {
      // ignore storage quota errors
    }
  }, [playlists]);

  const songPlaylist = mediaItems.filter((m) => m.type === 'song');
  const shortsList = mediaItems.filter((m) => m.type === 'short');

  const persistLastPlayback = useCallback((item: MediaItem, currentTime: number) => {
    if (!isPersistableMediaItem(item)) return;
    const now = Date.now();
    if (now - lastSavedTickRef.current < 1500 && currentTime > 0) return;
    lastSavedTickRef.current = now;
    try {
      const payload: LastPlaybackState = {
        itemId: item.id,
        currentTime: Math.max(0, Math.floor(currentTime)),
        updatedAt: now,
      };
      localStorage.setItem(LAST_PLAYBACK_STORAGE_KEY, JSON.stringify(payload));
    } catch {
      // ignore storage errors
    }
  }, []);

  const showTopMediaNotification = useCallback((item: MediaItem, playing: boolean) => {
    if (typeof window === 'undefined' || !('Notification' in window)) return;
    if (Notification.permission !== 'granted') return;
    try {
      const n = new Notification(playing ? `Playing: ${item.title}` : `Paused: ${item.title}`, {
        body: `${item.artist || item.sourceLabel || 'V2S Player'} · Tap to return to player`,
        tag: 'v2s-now-playing',
        silent: true,
        icon: item.thumbnail || undefined,
      });
      n.onclick = () => {
        window.focus();
        n.close();
      };
    } catch {
      // ignore notification errors on unsupported contexts
    }
  }, []);

  const syncMediaSession = useCallback(
    (item: MediaItem | null, playing: boolean, positionSec = 0, durationSec = 0) => {
      if (typeof navigator === 'undefined' || !('mediaSession' in navigator)) return;
      if (!item) {
        navigator.mediaSession.playbackState = 'none';
        return;
      }

      if (typeof MediaMetadata !== 'undefined') {
        navigator.mediaSession.metadata = new MediaMetadata({
          title: item.title,
          artist: item.artist || item.sourceLabel || 'V2S Player',
          album: item.album || 'V2S Background Player',
          artwork: item.thumbnail
            ? [{ src: item.thumbnail, sizes: '512x512', type: 'image/jpeg' }]
            : [],
        });
      }

      navigator.mediaSession.playbackState = playing ? 'playing' : 'paused';

      if (typeof navigator.mediaSession.setPositionState === 'function' && durationSec > 0 && Number.isFinite(durationSec)) {
        try {
          navigator.mediaSession.setPositionState({
            duration: durationSec,
            playbackRate: 1,
            position: Math.min(Math.max(0, positionSec), durationSec),
          });
        } catch {
          // ignore invalid position state
        }
      }
    },
    []
  );

  const resolvedAudioSrc = (song: MediaItem | null): string | undefined => {
    if (!song) return undefined;
    if (song.extractedAudioUrl) return song.extractedAudioUrl;
    if (song.extractedVideoUrl) return song.extractedVideoUrl;
    if (!song.embedUrl) return song.uri;
    return undefined;
  };

  const handlePlaySong = useCallback(
    async (song: MediaItem, startAtSec = 0) => {
      setAudioError(null);
      setResumeBanner(null);

      let targetSong = song;
      if (song.sourceType === 'link' && !song.extractedAudioUrl && !song.extractedVideoUrl && song.embedUrl) {
        const extracted = await extractPlayableMedia(song.uri);
        if (extracted.ok && (extracted.audioUrl || extracted.videoUrl)) {
          targetSong = {
            ...song,
            extractedVideoUrl: extracted.videoUrl,
            extractedAudioUrl: extracted.audioUrl || extracted.videoUrl,
            duration: extracted.duration || song.duration,
            thumbnail: song.thumbnail || extracted.thumbnailUrl,
          };
          setMediaItems((prev) => prev.map((m) => (m.id === song.id ? targetSong : m)));
        }
      }

      setCurrentSong(targetSong);
      persistLastPlayback(targetSong, startAtSec);

      const playableSrc = resolvedAudioSrc(targetSong);
      if (!playableSrc) {
        setIsPlayingAudio(false);
        syncMediaSession(targetSong, false, 0, targetSong.duration || 0);
        return;
      }

      setIsPlayingAudio(true);
      showTopMediaNotification(targetSong, true);
      const audio = audioRef.current;
      if (audio) {
        audio.src = playableSrc;
        if (startAtSec > 0) {
          const applySeek = () => {
            try {
              audio.currentTime = startAtSec;
            } catch {
              // ignore seek before ready
            }
            audio.removeEventListener('loadedmetadata', applySeek);
          };
          audio.addEventListener('loadedmetadata', applySeek);
        }
        audio.load();
        audio.play().catch(() => setIsPlayingAudio(false));
      }
    },
    [persistLastPlayback, showTopMediaNotification, syncMediaSession]
  );

  const handleToggleAudioPlay = useCallback(() => {
    const playableSrc = resolvedAudioSrc(currentSong);
    if (!audioRef.current || !currentSong || !playableSrc) return;
    setAudioError(null);
    if (audioRef.current.paused) {
      audioRef.current.play().catch(() => setIsPlayingAudio(false));
      setIsPlayingAudio(true);
    } else {
      audioRef.current.pause();
      setIsPlayingAudio(false);
    }
  }, [currentSong]);

  const handleRetryAudio = useCallback(() => {
    const playableSrc = resolvedAudioSrc(currentSong);
    if (!currentSong || !audioRef.current || !playableSrc) return;
    setAudioError(null);
    setIsPlayingAudio(true);
    audioRef.current.src = playableSrc;
    audioRef.current.load();
    audioRef.current.play().catch(() => setIsPlayingAudio(false));
  }, [currentSong]);

  const activePlaylistQueue = React.useMemo(() => {
    if (!activePlaylistId) return songPlaylist;
    const pl = playlists.find((p) => p.id === activePlaylistId);
    if (!pl) return songPlaylist;
    const resolved = pl.itemIds
      .map((id) => mediaItems.find((m) => m.id === id))
      .filter((m): m is MediaItem => Boolean(m));
    return resolved.length > 0 ? resolved : songPlaylist;
  }, [activePlaylistId, playlists, mediaItems, songPlaylist]);

  const handleNextSong = useCallback(() => {
    if (!currentSong) return;
    const queue =
      activePlaylistId && activePlaylistQueue.some((m) => m.id === currentSong.id)
        ? activePlaylistQueue
        : currentSong.type === 'song'
        ? songPlaylist
        : mediaItems.filter((m) => m.type !== 'short');
    if (queue.length === 0) return;
    const curIdx = queue.findIndex((s) => s.id === currentSong.id);
    const nextIdx = (curIdx + 1) % queue.length;
    handlePlaySong(queue[nextIdx]);
  }, [currentSong, activePlaylistId, activePlaylistQueue, songPlaylist, mediaItems, handlePlaySong]);

  const handlePrevSong = useCallback(() => {
    if (!currentSong) return;
    const queue =
      activePlaylistId && activePlaylistQueue.some((m) => m.id === currentSong.id)
        ? activePlaylistQueue
        : currentSong.type === 'song'
        ? songPlaylist
        : mediaItems.filter((m) => m.type !== 'short');
    if (queue.length === 0) return;
    const curIdx = queue.findIndex((s) => s.id === currentSong.id);
    const prevIdx = (curIdx - 1 + queue.length) % queue.length;
    handlePlaySong(queue[prevIdx]);
  }, [currentSong, activePlaylistId, activePlaylistQueue, songPlaylist, mediaItems, handlePlaySong]);

  // Background playback & OS MediaSession lockscreen/notification controls (always enabled by default)
  useEffect(() => {
    const handleVisibility = () => {
      if (document.hidden) {
        if (currentSong && isPlayingAudio) {
          showTopMediaNotification(currentSong, true);
        } else if (activeVideo) {
          showTopMediaNotification(activeVideo, true);
        }
      }
    };
    document.addEventListener('visibilitychange', handleVisibility);
    return () => document.removeEventListener('visibilitychange', handleVisibility);
  }, [currentSong, isPlayingAudio, activeVideo, showTopMediaNotification]);

  useEffect(() => {
    if (!currentSong) {
      if (!activeVideo) syncMediaSession(null, false);
      return;
    }

    syncMediaSession(
      currentSong,
      isPlayingAudio,
      audioRef.current?.currentTime || 0,
      audioRef.current?.duration || currentSong.duration || 0
    );

    if (typeof navigator === 'undefined' || !('mediaSession' in navigator)) return;
    try {
      navigator.mediaSession.setActionHandler('play', () => {
        audioRef.current?.play().catch(() => {});
        setIsPlayingAudio(true);
      });
      navigator.mediaSession.setActionHandler('pause', () => {
        audioRef.current?.pause();
        setIsPlayingAudio(false);
      });
      navigator.mediaSession.setActionHandler('nexttrack', handleNextSong);
      navigator.mediaSession.setActionHandler('previoustrack', handlePrevSong);
      navigator.mediaSession.setActionHandler('seekbackward', (details) => {
        if (audioRef.current) {
          audioRef.current.currentTime = Math.max(0, audioRef.current.currentTime - (details.seekOffset || 10));
        }
      });
      navigator.mediaSession.setActionHandler('seekforward', (details) => {
        if (audioRef.current) {
          audioRef.current.currentTime = Math.min(
            audioRef.current.duration || Infinity,
            audioRef.current.currentTime + (details.seekOffset || 10)
          );
        }
      });
      navigator.mediaSession.setActionHandler('seekto', (details) => {
        if (audioRef.current && typeof details.seekTime === 'number') {
          audioRef.current.currentTime = details.seekTime;
        }
      });
    } catch {
      // ignore unsupported MediaSession handlers
    }
  }, [currentSong, activeVideo, isPlayingAudio, handleNextSong, handlePrevSong, syncMediaSession]);

  const handleVideoPlaybackSync = useCallback(
    (item: MediaItem, playing: boolean, currentTime: number, duration: number) => {
      syncMediaSession(item, playing, currentTime, duration);
      persistLastPlayback(item, currentTime);
    },
    [syncMediaSession, persistLastPlayback]
  );

  const handlePlayVideoInBackground = (item: MediaItem, resumeTimeSec: number) => {
    if (typeof window !== 'undefined' && 'Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission().catch(() => {});
    }
    setActiveVideo(null);
    handlePlaySong(item, resumeTimeSec);
    showNotice(`Playing "${item.title}" in background with top notification`);
  };

  const handleToggleFavorite = (id: string) => {
    setMediaItems((prev) =>
      prev.map((item) =>
        item.id === id ? { ...item, isFavorite: !item.isFavorite } : item
      )
    );
    if (currentSong?.id === id) {
      setCurrentSong((prev) => (prev ? { ...prev, isFavorite: !prev.isFavorite } : null));
    }
  };

  const handleDeleteItem = (id: string) => {
    setMediaItems((prev) => prev.filter((item) => item.id !== id));
    setPlaylists((prev) =>
      prev.map((pl) => ({ ...pl, itemIds: pl.itemIds.filter((x) => x !== id) }))
    );
    if (currentSong?.id === id) {
      audioRef.current?.pause();
      setCurrentSong(null);
      setIsPlayingAudio(false);
      setAudioError(null);
    }
    if (activeVideo?.id === id) {
      setActiveVideo(null);
    }
    showNotice('Removed item from library');
  };

  const handleCreatePlaylist = (name: string, description: string) => {
    const newPl: Playlist = {
      id: `pl-${Date.now()}`,
      name,
      description,
      itemIds: [],
      createdAt: Date.now(),
      updatedAt: Date.now(),
    };
    setPlaylists((prev) => [newPl, ...prev]);
    showNotice(`Playlist "${name}" created`);
  };

  const handleAddToPlaylist = (itemId: string, playlistId: string) => {
    setPlaylists((prev) =>
      prev.map((pl) => {
        if (pl.id !== playlistId) return pl;
        if (pl.itemIds.includes(itemId)) return pl;
        return { ...pl, itemIds: [...pl.itemIds, itemId], updatedAt: Date.now() };
      })
    );
    const targetPl = playlists.find((p) => p.id === playlistId);
    showNotice(`Added to "${targetPl?.name || 'Playlist'}"`);
  };

  const handleRemoveFromPlaylist = (itemId: string, playlistId: string) => {
    setPlaylists((prev) =>
      prev.map((pl) =>
        pl.id === playlistId
          ? { ...pl, itemIds: pl.itemIds.filter((id) => id !== itemId), updatedAt: Date.now() }
          : pl
      )
    );
    showNotice('Removed track from playlist');
  };

  const handlePlayPlaylist = (pl: Playlist, startItem?: MediaItem) => {
    const resolved = pl.itemIds
      .map((id) => mediaItems.find((m) => m.id === id))
      .filter((m): m is MediaItem => Boolean(m));
    const target = startItem || resolved[0];
    if (!target) {
      showNotice('Playlist has no playable items', 'error');
      return;
    }
    setActivePlaylistId(pl.id);
    if (target.type === 'song') handlePlaySong(target);
    else if (target.type === 'short') {
      setSelectedShortId(target.id);
      setActiveTab('shorts');
    } else {
      setVideoResumeSec(0);
      setActiveVideo(target);
    }
  };

  const handleDeletePlaylist = (playlistId: string) => {
    setPlaylists((prev) => prev.filter((p) => p.id !== playlistId));
    if (activePlaylistId === playlistId) setActivePlaylistId(null);
    showNotice('Playlist deleted');
  };

  const handleImportPlaylistLink = async (
    rawUrl: string,
    customTitle?: string,
    preferredType?: MediaType,
    forceOverride = false
  ): Promise<{ ok: boolean; error?: string; allowForce?: boolean }> => {
    const parsed = parseMediaUrl(rawUrl, preferredType);
    const extracted = await extractPlaylistFromUrl(parsed.normalizedUrl, preferredType);

    if (extracted.ok && extracted.items.length > 0) {
      const now = Date.now();
      const itemIds: string[] = [];
      const newItemsToAdd: MediaItem[] = [];

      extracted.items.forEach((track, idx) => {
        const existing = mediaItems.find(
          (m) => m.uri.trim().toLowerCase() === track.url.trim().toLowerCase()
        );
        if (existing) {
          itemIds.push(existing.id);
          return;
        }
        const id = `link-${now}-${idx}-${Math.random().toString(36).slice(2, 6)}`;
        itemIds.push(id);
        newItemsToAdd.push({
          id,
          title: track.title,
          artist: track.artist || extracted.author || track.sourceLabel,
          duration: track.duration || 0,
          uri: track.url,
          embedUrl: track.embedUrl,
          extractedVideoUrl: track.videoUrl,
          extractedAudioUrl: track.audioUrl,
          thumbnail: track.thumbnailUrl,
          type: track.type,
          folder: extracted.title || track.sourceLabel,
          size: track.videoUrl || track.audioUrl ? `${track.sourceLabel} (In-App)` : track.sourceLabel,
          sourceType: 'link',
          sourceLabel: track.sourceLabel,
          isFavorite: false,
          createdAt: now - idx,
        });
      });

      // Pre-extract the first track so playback starts immediately
      if (newItemsToAdd.length > 0 && newItemsToAdd[0].embedUrl && !newItemsToAdd[0].extractedVideoUrl) {
        const firstStream = await extractPlayableMedia(newItemsToAdd[0].uri);
        if (firstStream.ok && (firstStream.videoUrl || firstStream.audioUrl)) {
          newItemsToAdd[0] = {
            ...newItemsToAdd[0],
            extractedVideoUrl: firstStream.videoUrl,
            extractedAudioUrl: firstStream.audioUrl || firstStream.videoUrl,
            duration: firstStream.duration || newItemsToAdd[0].duration,
          };
        }
      }

      const plId = `pl-${now}`;
      const newPlaylist: Playlist = {
        id: plId,
        name: customTitle || extracted.title || parsed.defaultTitle,
        description: extracted.sourceLabel || parsed.sourceLabel,
        itemIds,
        coverImage: extracted.thumbnailUrl || newItemsToAdd[0]?.thumbnail,
        sourceUrl: parsed.normalizedUrl,
        createdAt: now,
        updatedAt: now,
      };

      const mergedItems = [...newItemsToAdd, ...mediaItems];
      setMediaItems(mergedItems);
      setPlaylists((prev) => [newPlaylist, ...prev]);
      setActivePlaylistId(plId);

      const firstToPlay = mergedItems.find((m) => m.id === itemIds[0]);
      if (firstToPlay) {
        if (firstToPlay.type === 'song') handlePlaySong(firstToPlay);
        else if (firstToPlay.type === 'short') {
          setSelectedShortId(firstToPlay.id);
          setActiveTab('shorts');
        } else {
          setVideoResumeSec(0);
          setActiveVideo(firstToPlay);
        }
      }

      showNotice(`Imported "${newPlaylist.name}" (${itemIds.length} tracks)`);
      return { ok: true };
    }

    // Fallback for embed-only playlists (e.g. Spotify / private YouTube playlist)
    if ((extracted.ok || forceOverride) && (extracted.embedUrl || parsed.embedUrl)) {
      const now = Date.now();
      const id = `link-${now}-${Math.random().toString(36).slice(2, 6)}`;
      const fallbackItem: MediaItem = {
        id,
        title: customTitle || extracted.title || parsed.defaultTitle,
        artist: extracted.author || parsed.sourceLabel,
        duration: 0,
        uri: parsed.normalizedUrl,
        embedUrl: extracted.embedUrl || parsed.embedUrl,
        thumbnail: extracted.thumbnailUrl || parsed.thumbnailUrl,
        type: preferredType || parsed.inferredType,
        folder: extracted.sourceLabel || parsed.sourceLabel,
        size: extracted.sourceLabel || parsed.sourceLabel,
        sourceType: 'link',
        sourceLabel: extracted.sourceLabel || parsed.sourceLabel,
        isFavorite: false,
        createdAt: now,
      };
      const plId = `pl-${now}`;
      const newPlaylist: Playlist = {
        id: plId,
        name: fallbackItem.title,
        description: fallbackItem.sourceLabel,
        itemIds: [id],
        coverImage: fallbackItem.thumbnail,
        sourceUrl: parsed.normalizedUrl,
        createdAt: now,
        updatedAt: now,
      };
      setMediaItems((prev) => [fallbackItem, ...prev]);
      setPlaylists((prev) => [newPlaylist, ...prev]);
      setActivePlaylistId(plId);
      if (fallbackItem.type === 'song') handlePlaySong(fallbackItem);
      else {
        setVideoResumeSec(0);
        setActiveVideo(fallbackItem);
      }
      showNotice(`Saved playlist "${newPlaylist.name}"`);
      return { ok: true };
    }

    return {
      ok: false,
      allowForce: Boolean(parsed.embedUrl),
      error:
        'Could not fetch tracks from this playlist link. Make sure the playlist is public or check the URL.',
    };
  };

  const handleImportFiles = (newItems: MediaItem[]) => {
    setMediaItems((prev) => [...newItems, ...prev]);
    if (newItems.length > 0) {
      showNotice(`Loaded ${newItems.length} file${newItems.length === 1 ? '' : 's'} from device memory`);
      const first = newItems[0];
      if (first.type === 'song') handlePlaySong(first);
      else {
        setVideoResumeSec(0);
        setActiveVideo(first);
      }
    }
  };

  const handleSaveAndPlayLink = (item: MediaItem) => {
    setMediaItems((prev) => [item, ...prev]);
    showNotice(`Saved "${item.title}" locally`);
    if (item.type === 'song') {
      handlePlaySong(item);
    } else if (item.type === 'short') {
      setSelectedShortId(item.id);
      setActiveTab('shorts');
    } else {
      setVideoResumeSec(0);
      setActiveVideo(item);
    }
  };

  const handleUpdateLinkItem = (id: string, updated: MediaItem) => {
    setMediaItems((prev) => prev.map((m) => (m.id === id ? updated : m)));
    if (activeVideo?.id === id) {
      setActiveVideo(updated);
    }
    if (currentSong?.id === id) {
      handlePlaySong(updated);
    }
    showNotice(`Updated "${updated.title}"`);
  };

  const handleOpenEditModal = (item: MediaItem) => {
    setEditingItem(item);
    setShowStreamModal(true);
  };

  const handleQuickUrlChange = (val: string) => {
    setQuickUrl(val);
    setQuickError(null);
    setQuickUnverifiedPrompt(false);
    if (val.trim().length > 6) {
      setQuickType(parseMediaUrl(val).inferredType);
    }
  };

  const submitQuickLink = async (forceOverride: boolean) => {
    const validation = validateMediaUrl(quickUrl);
    if (!validation.valid) {
      setQuickError(validation.reason || 'Enter a valid video, song, or playlist link');
      return;
    }

    const normalizedUrl = validation.normalizedUrl;

    // Playlist link handling (YouTube, YouTube Music, Spotify, SoundCloud sets, M3U)
    if (isPlaylistUrl(normalizedUrl)) {
      setQuickSaving(true);
      setQuickError(null);
      setQuickUnverifiedPrompt(false);
      try {
        const res = await handleImportPlaylistLink(
          normalizedUrl,
          quickTitle.trim() || undefined,
          quickType,
          forceOverride
        );
        if (res.ok) {
          setQuickUrl('');
          setQuickTitle('');
        } else {
          setQuickError(res.error || 'Could not fetch this playlist link.');
          setQuickUnverifiedPrompt(Boolean(res.allowForce));
        }
      } finally {
        setQuickSaving(false);
      }
      return;
    }

    const duplicate = mediaItems.find(
      (item) => item.uri.trim().toLowerCase() === normalizedUrl.toLowerCase()
    );
    if (duplicate && !forceOverride) {
      showNotice(`Already saved "${duplicate.title}" — opening in-app`);
      if (duplicate.type === 'song') handlePlaySong(duplicate);
      else if (duplicate.type === 'short') {
        setSelectedShortId(duplicate.id);
        setActiveTab('shorts');
      } else {
        setVideoResumeSec(0);
        setActiveVideo(duplicate);
      }
      setQuickUrl('');
      setQuickTitle('');
      return;
    }

    setQuickSaving(true);
    setQuickError(null);
    try {
      const parsed = parseMediaUrl(normalizedUrl, quickType);
      let resolvedTitle = quickTitle.trim();
      let resolvedAuthor = parsed.sourceLabel;
      let resolvedThumb = parsed.thumbnailUrl;

      const [meta, extracted] = await Promise.all([
        fetchOEmbedMetadata(parsed.normalizedUrl),
        extractPlayableMedia(parsed.normalizedUrl),
      ]);

      if (meta.status === 'not_found' && !extracted.ok && !forceOverride) {
        setQuickError(
          'Could not fetch this video/song (404 / deleted or wrong link). Check the URL for typos.'
        );
        setQuickUnverifiedPrompt(true);
        setQuickSaving(false);
        return;
      }

      if (meta.status === 'ok') {
        if (!resolvedTitle && meta.title) resolvedTitle = meta.title;
        if (meta.author) resolvedAuthor = meta.author;
        if (!resolvedThumb && meta.thumbnailUrl) resolvedThumb = meta.thumbnailUrl;
      }
      if (extracted.ok) {
        if (!resolvedTitle && extracted.title) resolvedTitle = extracted.title;
        if (extracted.author) resolvedAuthor = extracted.author;
        if (!resolvedThumb && extracted.thumbnailUrl) resolvedThumb = extracted.thumbnailUrl;
      }

      const item: MediaItem = {
        id: `link-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
        title: resolvedTitle || parsed.defaultTitle,
        artist: resolvedAuthor,
        duration: extracted.duration || 0,
        uri: parsed.normalizedUrl,
        embedUrl: parsed.embedUrl,
        extractedVideoUrl: extracted.videoUrl,
        extractedAudioUrl: extracted.audioUrl,
        thumbnail: resolvedThumb,
        type: quickType,
        folder: parsed.sourceLabel,
        size: extracted.videoUrl || extracted.audioUrl
          ? `${parsed.sourceLabel} (In-App)`
          : parsed.embedUrl
          ? parsed.sourceLabel
          : 'Saved Link',
        sourceType: 'link',
        sourceLabel: parsed.sourceLabel,
        isFavorite: false,
        createdAt: Date.now(),
      };

      handleSaveAndPlayLink(item);
      setQuickUrl('');
      setQuickTitle('');
      setQuickUnverifiedPrompt(false);
    } finally {
      setQuickSaving(false);
    }
  };

  const handleQuickAddSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await submitQuickLink(false);
  };

  return (
    <div className="min-h-screen bg-transparent text-slate-100 flex flex-col font-sans pb-36 md:pb-24">
      <audio
        ref={audioRef}
        src={resolvedAudioSrc(currentSong)}
        preload="metadata"
        onCanPlay={() => setAudioError(null)}
        onPlaying={() => {
          setAudioError(null);
          setIsPlayingAudio(true);
          if (currentSong) showTopMediaNotification(currentSong, true);
        }}
        onPause={() => setIsPlayingAudio(false)}
        onTimeUpdate={() => {
          if (currentSong && audioRef.current) {
            persistLastPlayback(currentSong, audioRef.current.currentTime);
            syncMediaSession(
              currentSong,
              !audioRef.current.paused,
              audioRef.current.currentTime,
              audioRef.current.duration || currentSong.duration || 0
            );
          }
        }}
        onEnded={handleNextSong}
        onError={() => {
          if (!currentSong || !resolvedAudioSrc(currentSong)) return;
          if ((currentSong.extractedAudioUrl || currentSong.extractedVideoUrl) && currentSong.embedUrl) {
            // Fall back to clean audio embed deck if extracted CDN audio expired
            const fallbackSong: MediaItem = {
              ...currentSong,
              extractedAudioUrl: undefined,
              extractedVideoUrl: undefined,
            };
            setMediaItems((prev) => prev.map((m) => (m.id === currentSong.id ? fallbackSong : m)));
            setCurrentSong(fallbackSong);
            setIsPlayingAudio(false);
            return;
          }
          setIsPlayingAudio(false);
          setAudioError(describeMediaPlaybackError(audioRef.current?.error?.code, currentSong));
        }}
      />

      {appNotice && (
        <div
          className={`fixed top-16 right-3 sm:right-4 z-50 max-w-[calc(100vw-1.5rem)] sm:max-w-sm px-4 py-2.5 rounded-xl border backdrop-blur-md shadow-2xl flex items-center gap-2.5 text-xs font-semibold v2s-animate-toast ${
            appNotice.tone === 'error'
              ? 'bg-red-950/95 border-red-500/40 text-red-200'
              : 'bg-slate-900/95 border-indigo-500/40 text-slate-100'
          }`}
        >
          {appNotice.tone === 'error' ? (
            <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />
          ) : (
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          )}
          <span className="truncate">{appNotice.text}</span>
          <button
            type="button"
            onClick={() => setAppNotice(null)}
            className="text-slate-400 hover:text-white ml-1 shrink-0"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>
      )}

      <Navbar
        activeTab={activeTab}
        onTabChange={setActiveTab}
        onOpenStreamModal={() => {
          setEditingItem(null);
          setShowStreamModal(true);
        }}
      />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-5 sm:py-6 space-y-5 sm:space-y-6">
        {/* Resume Last Played Banner */}
        {resumeBanner && (
          <div className="rounded-2xl bg-indigo-950/55 border border-indigo-500/35 px-4 py-3 flex flex-wrap items-center justify-between gap-3 v2s-animate-fade">
            <div className="flex items-center gap-3 min-w-0">
              <div className="w-9 h-9 rounded-xl bg-indigo-600/25 text-indigo-300 flex items-center justify-center shrink-0">
                <RotateCcw className="w-4 h-4" />
              </div>
              <div className="min-w-0">
                <p className="text-xs font-semibold text-indigo-300">Continue playing</p>
                <p className="text-xs sm:text-sm font-bold text-white truncate">
                  {resumeBanner.item.title}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2 shrink-0 ml-auto">
              <button
                type="button"
                onClick={() => {
                  const { item, currentTime } = resumeBanner;
                  setResumeBanner(null);
                  if (item.type === 'song') handlePlaySong(item, currentTime);
                  else if (item.type === 'short') {
                    setSelectedShortId(item.id);
                    setActiveTab('shorts');
                  } else {
                    setVideoResumeSec(currentTime);
                    setActiveVideo(item);
                  }
                }}
                className="min-h-[38px] px-3.5 py-1.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold flex items-center gap-1.5 shadow-md shadow-indigo-600/25 transition-colors"
              >
                <Play className="w-3.5 h-3.5 fill-current" />
                Resume
              </button>
              <button
                type="button"
                onClick={() => setResumeBanner(null)}
                className="min-h-[38px] min-w-[38px] flex items-center justify-center rounded-xl text-slate-400 hover:text-white hover:bg-slate-800/70 transition-colors"
                aria-label="Dismiss resume banner"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}

        {/* Streamlined Quick Add Bar (Video, Song, Short, or Playlist Link) */}
        {activeTab !== 'shorts' && (
          <section className="rounded-2xl bg-slate-900/70 border border-slate-800/90 p-4 sm:p-5 space-y-3.5">
            <div className="flex flex-wrap items-center justify-between gap-2.5">
              <h1 className="text-sm sm:text-base font-bold text-white tracking-tight">
                Add Video, Song, or Playlist Link
              </h1>

              <div className="flex items-center gap-2 shrink-0">
                <button
                  type="button"
                  onClick={() => setShowImportModal(true)}
                  className="min-h-[38px] px-3 py-1.5 rounded-xl bg-slate-950 hover:bg-slate-800 text-slate-200 border border-slate-800 text-xs font-semibold flex items-center gap-1.5 transition-colors whitespace-nowrap"
                >
                  <FolderOpen className="w-3.5 h-3.5 text-emerald-400" />
                  Device Files
                </button>
                <button
                  type="button"
                  onClick={() => setShowEqModal(true)}
                  className="min-h-[38px] px-3 py-1.5 rounded-xl bg-slate-950 hover:bg-slate-800 text-slate-200 border border-slate-800 text-xs font-semibold flex items-center gap-1.5 transition-colors whitespace-nowrap"
                >
                  <Sliders className="w-3.5 h-3.5 text-indigo-400" />
                  Equalizer
                </button>
              </div>
            </div>

            <form onSubmit={handleQuickAddSubmit} className="grid grid-cols-1 lg:grid-cols-12 gap-2.5 items-stretch">
              <div className="lg:col-span-5 relative">
                <Link2 className="w-4 h-4 text-slate-500 absolute left-3.5 top-3.5" />
                <input
                  type="text"
                  required
                  value={quickUrl}
                  onChange={(e) => handleQuickUrlChange(e.target.value)}
                  placeholder="Paste YouTube, Playlist, Music, Spotify, or MP4/MP3 link..."
                  className={`w-full min-h-[44px] pl-10 pr-3.5 py-2.5 bg-slate-950 border rounded-xl text-xs sm:text-sm text-white placeholder-slate-500 focus:outline-none font-mono transition-colors ${
                    quickError
                      ? 'border-red-500/80 focus:border-red-400'
                      : 'border-slate-800 focus:border-indigo-500'
                  }`}
                />
              </div>

              <div className="lg:col-span-3">
                <input
                  type="text"
                  value={quickTitle}
                  onChange={(e) => setQuickTitle(e.target.value)}
                  placeholder="Title (auto-detected)"
                  className="w-full min-h-[44px] px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs sm:text-sm text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors"
                />
              </div>

              <div className="lg:col-span-2 flex bg-slate-950 p-1 rounded-xl border border-slate-800 min-h-[44px]">
                {[
                  { id: 'video' as MediaType, label: 'Video', icon: Film },
                  { id: 'short' as MediaType, label: 'Short', icon: Flame },
                  { id: 'song' as MediaType, label: 'Song', icon: Music },
                ].map((opt) => {
                  const Icon = opt.icon;
                  const active = quickType === opt.id;
                  return (
                    <button
                      key={opt.id}
                      type="button"
                      onClick={() => setQuickType(opt.id)}
                      className={`flex-1 py-1.5 px-2 rounded-lg text-[11px] font-semibold flex items-center justify-center gap-1 transition-colors whitespace-nowrap ${
                        active
                          ? 'bg-indigo-600 text-white'
                          : 'text-slate-400 hover:text-white'
                      }`}
                    >
                      <Icon className="w-3 h-3" />
                      {opt.label}
                    </button>
                  );
                })}
              </div>

              <button
                type="submit"
                disabled={quickSaving}
                className="lg:col-span-2 min-h-[44px] px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 disabled:opacity-60 text-white text-xs sm:text-sm font-semibold flex items-center justify-center gap-1.5 shadow-md shadow-indigo-600/25 transition-colors active:scale-[0.98] whitespace-nowrap"
              >
                <Plus className="w-4 h-4" />
                {quickSaving ? 'Loading...' : 'Add & Play'}
              </button>
            </form>

            {quickUrl.trim().length > 6 && !quickError && isPlaylistUrl(quickUrl) && (
              <p className="text-xs text-emerald-400 flex items-center gap-1.5">
                <CheckCircle2 className="w-3.5 h-3.5 shrink-0" />
                Playlist link detected — tracks will be imported &amp; queued automatically
              </p>
            )}

            {quickError && (
              <div className="flex flex-wrap items-center justify-between gap-2 rounded-xl bg-red-500/10 border border-red-500/30 px-3.5 py-2.5 text-xs text-red-200 v2s-animate-fade">
                <div className="flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />
                  <span>{quickError}</span>
                </div>
                {quickUnverifiedPrompt && (
                  <button
                    type="button"
                    onClick={() => submitQuickLink(true)}
                    className="px-3 py-1 rounded-lg bg-red-500/20 hover:bg-red-500/30 text-red-100 font-semibold transition-colors whitespace-nowrap"
                  >
                    Save Anyway
                  </button>
                )}
              </div>
            )}
          </section>
        )}

        {activeTab === 'shorts' ? (
          <ShortsPlayerView
            shorts={shortsList}
            selectedShortId={selectedShortId}
            onAddLink={() => {
              setEditingItem(null);
              setShowStreamModal(true);
            }}
            onEditItem={handleOpenEditModal}
            onDeleteItem={handleDeleteItem}
            onOpenSong={(songId) => {
              const found = mediaItems.find((m) => m.id === songId);
              if (found) handlePlaySong(found);
            }}
          />
        ) : (
          <LibraryView
            mediaItems={mediaItems}
            playlists={playlists}
            onPlayVideo={(item) => {
              setActivePlaylistId(null);
              setVideoResumeSec(0);
              setActiveVideo(item);
            }}
            onPlaySong={(item) => {
              setActivePlaylistId(null);
              handlePlaySong(item);
            }}
            onSelectShort={(item) => {
              setSelectedShortId(item.id);
              setActiveTab('shorts');
            }}
            onToggleFavorite={handleToggleFavorite}
            onEditItem={handleOpenEditModal}
            onDeleteItem={handleDeleteItem}
            onCreatePlaylist={handleCreatePlaylist}
            onImportPlaylistLink={handleImportPlaylistLink}
            onAddToPlaylist={handleAddToPlaylist}
            onRemoveFromPlaylist={handleRemoveFromPlaylist}
            onPlayPlaylist={handlePlayPlaylist}
            onDeletePlaylist={handleDeletePlaylist}
            onOpenAddLink={() => {
              setEditingItem(null);
              setShowStreamModal(true);
            }}
            onOpenDeviceImport={() => setShowImportModal(true)}
            activeTab={activeTab}
            onTabChange={setActiveTab}
          />
        )}

        <footer className="pt-4 border-t border-slate-900 flex items-center justify-between text-xs text-slate-500">
          <span>V2S Player</span>
          <button
            type="button"
            onClick={() => setShowApkModal(true)}
            className="text-slate-400 hover:text-white transition-colors"
          >
            Download Android APK
          </button>
        </footer>
      </main>

      <AudioPlayerDeck
        currentSong={currentSong}
        playlist={activePlaylistId ? activePlaylistQueue : songPlaylist}
        isPlaying={isPlayingAudio}
        audioError={audioError}
        onTogglePlay={handleToggleAudioPlay}
        onRetryAudio={handleRetryAudio}
        onNext={handleNextSong}
        onPrev={handlePrevSong}
        onSelectSong={(song) => handlePlaySong(song)}
        onToggleFavorite={handleToggleFavorite}
        onEditSong={handleOpenEditModal}
        onDeleteSong={handleDeleteItem}
        onOpenEqualizer={() => setShowEqModal(true)}
        audioRef={audioRef}
        currentGains={eqGains}
        bassBoost={bassBoost}
      />

      <VideoPlayerModal
        item={activeVideo}
        resumeTimeSec={videoResumeSec}
        onClose={() => setActiveVideo(null)}
        onOpenEqualizer={() => setShowEqModal(true)}
        onEditItem={handleOpenEditModal}
        onDeleteItem={handleDeleteItem}
        onPlayInBackground={handlePlayVideoInBackground}
        onPlaybackSync={handleVideoPlaybackSync}
      />

      <EqualizerModal
        isOpen={showEqModal}
        onClose={() => setShowEqModal(false)}
        currentGains={eqGains}
        onGainsChange={setEqGains}
        bassBoost={bassBoost}
        onBassBoostChange={setBassBoost}
        virtualizer={virtualizer}
        onVirtualizerChange={setVirtualizer}
        enabled={eqEnabled}
        onToggleEnabled={() => setEqEnabled(!eqEnabled)}
      />

      <ApkHubModal
        isOpen={showApkModal}
        onClose={() => setShowApkModal(false)}
      />

      <NetworkStreamModal
        isOpen={showStreamModal}
        onClose={() => {
          setShowStreamModal(false);
          setEditingItem(null);
        }}
        onPlayStream={handleSaveAndPlayLink}
        onImportPlaylistLink={handleImportPlaylistLink}
        editingItem={editingItem}
        onUpdateStream={handleUpdateLinkItem}
        existingItems={mediaItems}
      />

      <FileImportModal
        isOpen={showImportModal}
        onClose={() => setShowImportModal(false)}
        onImportFiles={handleImportFiles}
      />
    </div>
  );
}
