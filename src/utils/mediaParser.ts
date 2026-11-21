import { MediaItem, MediaType } from '../types/media';

export interface ParsedMediaUrl {
  normalizedUrl: string;
  embedUrl?: string;
  thumbnailUrl?: string;
  inferredType: MediaType;
  sourceLabel: string;
  defaultTitle: string;
}

export interface UrlValidationResult {
  valid: boolean;
  normalizedUrl: string;
  reason?: string;
}

export interface OEmbedResult {
  status: 'ok' | 'not_found' | 'embed_restricted' | 'unavailable';
  title?: string;
  author?: string;
  thumbnailUrl?: string;
}

export function normalizeMediaUrl(rawUrl: string): string {
  const trimmed = rawUrl.trim();
  if (!trimmed) return '';
  if (/^(https?:|rtsp:|rtmp:|blob:)/i.test(trimmed)) {
    return trimmed;
  }
  return `https://${trimmed}`;
}

export function extractYouTubeVideoId(rawUrl: string): string | null {
  const match = rawUrl.match(
    /(?:youtube\.com\/watch\?(?:.*&)?v=|youtube\.com\/embed\/|youtube\.com\/v\/|youtube\.com\/shorts\/|youtube\.com\/live\/|music\.youtube\.com\/watch\?(?:.*&)?v=|youtu\.be\/)([A-Za-z0-9_-]{11})(?:[?&#/]|$)/i
  );
  return match ? match[1] : null;
}

export function extractYouTubePlaylistId(rawUrl: string): string | null {
  try {
    const normalized = normalizeMediaUrl(rawUrl);
    const parsed = new URL(normalized);
    const host = parsed.hostname.toLowerCase();
    if (host === 'youtube.com' || host.endsWith('.youtube.com') || host === 'youtu.be') {
      const listId = parsed.searchParams.get('list');
      if (listId && /^[A-Za-z0-9_-]{10,}$/.test(listId)) {
        return listId;
      }
    }
  } catch {
    // fallback regex
  }
  const match = rawUrl.match(/[?&]list=([A-Za-z0-9_-]{10,})(?:&|#|$)/i);
  return match ? match[1] : null;
}

export function isPlaylistUrl(rawUrl: string): boolean {
  const normalized = normalizeMediaUrl(rawUrl);
  try {
    const parsed = new URL(normalized);
    const host = parsed.hostname.toLowerCase();
    const path = parsed.pathname.toLowerCase();

    if (host === 'youtube.com' || host.endsWith('.youtube.com') || host === 'youtu.be') {
      const listId = extractYouTubePlaylistId(normalized);
      if (!listId) return false;
      // Treat /playlist?list=... as a playlist, OR any YouTube link with list= if there's no single video ID
      if (path.startsWith('/playlist') || !extractYouTubeVideoId(normalized)) return true;
      return false;
    }
    if (host === 'open.spotify.com') {
      return /^\/(intl-[a-z-]+\/)?(playlist|album)\/[A-Za-z0-9]+/i.test(parsed.pathname);
    }
    if (host === 'soundcloud.com' || host === 'www.soundcloud.com') {
      return /\/sets\//i.test(path);
    }
    if (host === 'vimeo.com' || host.endsWith('.vimeo.com')) {
      return /^\/showcase\/\d+/i.test(path);
    }
    if (/\.m3u(\?.*)?$/i.test(path)) {
      return true;
    }
  } catch {
    return false;
  }
  return false;
}

export function extractVimeoId(rawUrl: string): string | null {
  const match = rawUrl.match(/vimeo\.com\/(?:channels\/[^/]+\/|groups\/[^/]+\/videos\/|video\/)?(\d{5,})(?:[?#/]|$)/i);
  return match ? match[1] : null;
}

export function buildCleanYouTubeEmbedUrl(videoId: string, origin?: string): string {
  const safeOrigin =
    origin || (typeof window !== 'undefined' ? window.location.origin : undefined);
  const originParam = safeOrigin ? `&origin=${encodeURIComponent(safeOrigin)}` : '';
  return `https://www.youtube-nocookie.com/embed/${videoId}?enablejsapi=1&autoplay=1&controls=0&rel=0&modestbranding=1&iv_load_policy=3&playsinline=1&fs=0&disablekb=1${originParam}`;
}

export function buildCleanYouTubePlaylistEmbedUrl(playlistId: string, origin?: string): string {
  const safeOrigin =
    origin || (typeof window !== 'undefined' ? window.location.origin : undefined);
  const originParam = safeOrigin ? `&origin=${encodeURIComponent(safeOrigin)}` : '';
  return `https://www.youtube-nocookie.com/embed/videoseries?list=${encodeURIComponent(
    playlistId
  )}&enablejsapi=1&autoplay=1&controls=0&rel=0&modestbranding=1&iv_load_policy=3&playsinline=1&fs=0&disablekb=1${originParam}`;
}

export function getCleanEmbedUrl(item: { uri: string; embedUrl?: string }): string | undefined {
  const ytId =
    extractYouTubeVideoId(item.uri) ||
    (item.embedUrl ? extractYouTubeVideoId(item.embedUrl) : null);
  if (ytId) return buildCleanYouTubeEmbedUrl(ytId);
  const ytList =
    extractYouTubePlaylistId(item.uri) ||
    (item.embedUrl ? extractYouTubePlaylistId(item.embedUrl) : null);
  if (ytList) return buildCleanYouTubePlaylistEmbedUrl(ytList);
  return item.embedUrl;
}

export function validateMediaUrl(rawUrl: string): UrlValidationResult {
  const trimmed = rawUrl.trim();
  if (!trimmed) {
    return { valid: false, normalizedUrl: '', reason: 'Please paste a video or song link.' };
  }

  if (/\s/.test(trimmed)) {
    return {
      valid: false,
      normalizedUrl: trimmed,
      reason: 'Link contains spaces. Copy and paste a single valid URL without spaces.',
    };
  }

  if (/^(javascript|data|vbscript|file):/i.test(trimmed)) {
    return {
      valid: false,
      normalizedUrl: trimmed,
      reason: 'Unsupported link protocol. Use an http://, https://, rtsp://, or rtmp:// media link.',
    };
  }

  const normalizedUrl = normalizeMediaUrl(trimmed);

  let parsedUrl: URL;
  try {
    parsedUrl = new URL(normalizedUrl);
  } catch {
    return {
      valid: false,
      normalizedUrl,
      reason: 'Invalid link format. Check for typos (e.g. https://youtube.com/watch?v=... or .mp4/.mp3).',
    };
  }

  const host = parsedUrl.hostname.toLowerCase();
  const isIpOrLocal = host === 'localhost' || /^\d{1,3}(\.\d{1,3}){3}$/.test(host);
  const hasTld = host.includes('.') && !host.startsWith('.') && !host.endsWith('.') && host.split('.').pop()!.length >= 2;
  if (!isIpOrLocal && !hasTld) {
    return {
      valid: false,
      normalizedUrl,
      reason: 'Link does not have a valid website domain (e.g. youtube.com, vimeo.com, or example.com/video.mp4).',
    };
  }

  const lower = normalizedUrl.toLowerCase();
  const pathSegments = parsedUrl.pathname.split('/').filter(Boolean);

  // Strict YouTube validation (supports single videos AND playlist links)
  if (host === 'youtu.be' || host.endsWith('.youtu.be')) {
    const id = extractYouTubeVideoId(normalizedUrl);
    const listId = extractYouTubePlaylistId(normalizedUrl);
    if (!id && !listId) {
      return {
        valid: false,
        normalizedUrl,
        reason: 'Incomplete youtu.be link. It must include an 11-character video ID or playlist ID.',
      };
    }
  } else if (host === 'youtube.com' || host.endsWith('.youtube.com')) {
    const isWatch = parsedUrl.pathname === '/watch';
    const isPlaylistPath = parsedUrl.pathname.startsWith('/playlist');
    const isShorts = parsedUrl.pathname.startsWith('/shorts');
    const isEmbed = parsedUrl.pathname.startsWith('/embed');
    const isLive = parsedUrl.pathname.startsWith('/live');
    const id = extractYouTubeVideoId(normalizedUrl);
    const listId = extractYouTubePlaylistId(normalizedUrl);

    if (isPlaylistPath && !listId) {
      return {
        valid: false,
        normalizedUrl,
        reason: 'Incomplete YouTube playlist link. Include the list=... playlist ID (e.g. youtube.com/playlist?list=PL...).',
      };
    }
    if (!id && !listId && (isWatch || isShorts || isEmbed || isLive || pathSegments.length === 0)) {
      return {
        valid: false,
        normalizedUrl,
        reason: 'Incomplete YouTube link. Paste a video, Short, Music track, or Playlist link (e.g. youtube.com/playlist?list=...).',
      };
    }
    if (!id && !listId) {
      return {
        valid: false,
        normalizedUrl,
        reason: 'Could not find a valid YouTube video or playlist ID in this URL. Check the link and try again.',
      };
    }
  }

  // Strict Vimeo validation
  if (host === 'vimeo.com' || host.endsWith('.vimeo.com')) {
    const isShowcase = /^\/showcase\/\d+/i.test(parsedUrl.pathname);
    if (!extractVimeoId(normalizedUrl) && !isShowcase) {
      return {
        valid: false,
        normalizedUrl,
        reason: 'Incomplete Vimeo link. Include the numeric video ID (e.g. https://vimeo.com/76979871).',
      };
    }
  }

  // Strict Spotify validation
  if (host === 'open.spotify.com') {
    const hasSpotifyResource = /^\/(intl-[a-z-]+\/)?(track|album|playlist|episode|show)\/[A-Za-z0-9]+/i.test(
      parsedUrl.pathname
    );
    if (!hasSpotifyResource) {
      return {
        valid: false,
        normalizedUrl,
        reason: 'Incomplete Spotify link. Paste a specific track, album, or playlist URL.',
      };
    }
  }

  // Strict SoundCloud validation
  if (host === 'soundcloud.com' || host === 'www.soundcloud.com') {
    if (pathSegments.length < 2) {
      return {
        valid: false,
        normalizedUrl,
        reason: 'Incomplete SoundCloud link. Include the artist and track path (e.g. soundcloud.com/artist/track).',
      };
    }
  }

  // Reject bare social homepages without a post/reel/video path
  if (
    (host.endsWith('instagram.com') || host.endsWith('tiktok.com') || host.endsWith('facebook.com')) &&
    pathSegments.length === 0 &&
    !lower.includes('?v=')
  ) {
    return {
      valid: false,
      normalizedUrl,
      reason: 'Paste a direct video or reel link instead of the website homepage.',
    };
  }

  return { valid: true, normalizedUrl };
}

export function parseMediaUrl(
  rawUrl: string,
  preferredType?: MediaType
): ParsedMediaUrl {
  const normalizedUrl = normalizeMediaUrl(rawUrl);
  const lower = normalizedUrl.toLowerCase();

  const ytId = extractYouTubeVideoId(normalizedUrl);
  const ytPlaylistId = extractYouTubePlaylistId(normalizedUrl);
  const vimeoId = extractVimeoId(normalizedUrl);
  const isAudioExt = /\.(mp3|wav|ogg|m4a|flac|aac|opus|wma)(\?.*)?(#.*)?$/i.test(lower);
  const isShorts = lower.includes('/shorts/');
  const isYtMusic = lower.includes('music.youtube.com');
  const isSoundCloud = lower.includes('soundcloud.com');
  const isSpotify = lower.includes('open.spotify.com');

  let inferredType: MediaType = preferredType || 'video';
  if (!preferredType) {
    if (isShorts) inferredType = 'short';
    else if (isYtMusic || isSoundCloud || isSpotify || isAudioExt) inferredType = 'song';
    else inferredType = 'video';
  }

  let embedUrl: string | undefined;
  let thumbnailUrl: string | undefined;
  let sourceLabel = 'Web Link';

  if (ytId) {
    embedUrl = buildCleanYouTubeEmbedUrl(ytId);
    thumbnailUrl = `https://img.youtube.com/vi/${ytId}/hqdefault.jpg`;
    sourceLabel = isYtMusic
      ? 'YouTube Music'
      : isShorts
      ? 'YouTube Shorts'
      : 'YouTube';
  } else if (ytPlaylistId) {
    embedUrl = buildCleanYouTubePlaylistEmbedUrl(ytPlaylistId);
    sourceLabel = isYtMusic ? 'YouTube Music Playlist' : 'YouTube Playlist';
  } else if (vimeoId) {
    embedUrl = `https://player.vimeo.com/video/${vimeoId}?autoplay=1`;
    sourceLabel = 'Vimeo';
  } else if (isSoundCloud) {
    embedUrl = `https://w.soundcloud.com/player/?url=${encodeURIComponent(normalizedUrl)}&auto_play=true&visual=true`;
    sourceLabel = lower.includes('/sets/') ? 'SoundCloud Playlist' : 'SoundCloud';
  } else if (isSpotify) {
    embedUrl = normalizedUrl.includes('/embed/')
      ? normalizedUrl
      : normalizedUrl.replace('open.spotify.com/', 'open.spotify.com/embed/');
    sourceLabel = lower.includes('/playlist/') || lower.includes('/album/') ? 'Spotify Playlist' : 'Spotify';
  } else {
    try {
      sourceLabel = new URL(normalizedUrl).hostname.replace(/^www\./, '');
    } catch {
      sourceLabel = 'Direct Stream';
    }
  }

  let defaultTitle = `${sourceLabel} ${inferredType === 'song' ? 'Track' : inferredType === 'short' ? 'Short' : 'Video'}`;
  if (ytId) {
    defaultTitle = `${sourceLabel} (${ytId})`;
  } else if (ytPlaylistId) {
    defaultTitle = `${sourceLabel} (${ytPlaylistId.slice(0, 12)})`;
  } else {
    try {
      const pathSeg = new URL(normalizedUrl).pathname.split('/').filter(Boolean).pop();
      if (pathSeg) {
        const cleaned = decodeURIComponent(pathSeg).replace(/\.[^/.]+$/, '').replace(/[-_]+/g, ' ').trim();
        if (cleaned.length > 2) defaultTitle = cleaned.slice(0, 64);
      }
    } catch {
      // keep fallback title
    }
  }

  return {
    normalizedUrl,
    embedUrl,
    thumbnailUrl,
    inferredType,
    sourceLabel,
    defaultTitle,
  };
}

export async function fetchOEmbedMetadata(rawUrl: string): Promise<OEmbedResult> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 4000);
  try {
    const ytId = extractYouTubeVideoId(rawUrl);
    if (ytId) {
      const canonicalYt = `https://www.youtube.com/watch?v=${ytId}`;
      const ytRes = await fetch(
        `https://www.youtube.com/oembed?url=${encodeURIComponent(canonicalYt)}&format=json`,
        { signal: controller.signal }
      );
      if (ytRes.status === 404 || ytRes.status === 400) {
        return { status: 'not_found' };
      }
      if (ytRes.status === 401 || ytRes.status === 403) {
        return { status: 'embed_restricted' };
      }
      if (ytRes.ok) {
        const ytData = await ytRes.json();
        return {
          status: 'ok',
          title: typeof ytData.title === 'string' ? ytData.title : undefined,
          author: typeof ytData.author_name === 'string' ? ytData.author_name : undefined,
          thumbnailUrl: typeof ytData.thumbnail_url === 'string' ? ytData.thumbnail_url : undefined,
        };
      }
    }

    const res = await fetch(
      `https://noembed.com/embed?url=${encodeURIComponent(rawUrl)}`,
      { signal: controller.signal }
    );
    if (!res.ok) return { status: 'unavailable' };
    const data = await res.json();
    if (!data || data.error) {
      const errText = typeof data?.error === 'string' ? data.error.toLowerCase() : '';
      if (errText.includes('404') || errText.includes('not found')) {
        return { status: 'not_found' };
      }
      if (errText.includes('401') || errText.includes('unauthorized') || errText.includes('forbidden')) {
        return { status: 'embed_restricted' };
      }
      return { status: 'unavailable' };
    }
    return {
      status: 'ok',
      title: typeof data.title === 'string' ? data.title : undefined,
      author: typeof data.author_name === 'string' ? data.author_name : undefined,
      thumbnailUrl: typeof data.thumbnail_url === 'string' ? data.thumbnail_url : undefined,
    };
  } catch {
    return { status: 'unavailable' };
  } finally {
    clearTimeout(timer);
  }
}

export function describeMediaPlaybackError(code: number | undefined, item: MediaItem): string {
  const isDevice = item.sourceType === 'device' || item.uri.startsWith('blob:');
  switch (code) {
    case 1:
      return 'Playback was interrupted before loading finished. Tap Retry to load again.';
    case 2:
      return isDevice
        ? 'Could not read this file from device memory.'
        : 'Network error while fetching media from this link. Check your internet connection or the URL.';
    case 3:
      return 'The media file could not be decoded by this browser. Try another format or open externally.';
    case 4:
      return isDevice
        ? 'This local file format is unsupported or the file reference expired — re-import from Device Memory.'
        : 'Could not fetch video/audio from this link (404 not found, blocked by host CORS, or not a direct media stream).';
    default:
      return isDevice
        ? 'Failed to load this local media file.'
        : 'Could not fetch media from this link. Verify the URL or open it in a new tab.';
  }
}

export function isPersistableMediaItem(item: MediaItem): boolean {
  return item.sourceType !== 'device' && !item.uri.startsWith('blob:');
}

export interface ExtractedStreamInfo {
  ok: boolean;
  videoUrl?: string;
  audioUrl?: string;
  title?: string;
  author?: string;
  thumbnailUrl?: string;
  duration?: number;
}

export async function extractPlayableMedia(rawUrl: string): Promise<ExtractedStreamInfo> {
  const normalizedUrl = normalizeMediaUrl(rawUrl);
  if (!normalizedUrl || normalizedUrl.startsWith('blob:')) {
    return { ok: false };
  }

  // Direct media files are already playable natively without extraction
  const lower = normalizedUrl.toLowerCase().split('?')[0].split('#')[0];
  if (/\.(mp4|webm|ogg|mov|m4v|mp3|wav|m4a|aac|flac|opus)$/i.test(lower)) {
    const isAudio = /\.(mp3|wav|m4a|aac|flac|opus)$/i.test(lower);
    return {
      ok: true,
      videoUrl: isAudio ? undefined : normalizedUrl,
      audioUrl: normalizedUrl,
    };
  }

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 9000);
  try {
    const res = await fetch(`/api/extract?url=${encodeURIComponent(normalizedUrl)}`, {
      signal: controller.signal,
    });
    if (res.ok) {
      const data = (await res.json()) as ExtractedStreamInfo;
      if (data?.ok && (data.videoUrl || data.audioUrl)) {
        return data;
      }
    }
  } catch {
    // fallback below
  } finally {
    clearTimeout(timer);
  }

  // Client-side Piped JSON fallback if backend middleware is absent
  const ytId = extractYouTubeVideoId(normalizedUrl);
  if (ytId) {
    for (const host of ['https://pipedapi.kavin.rocks', 'https://pipedapi.adminforge.de']) {
      try {
        const c = new AbortController();
        const t = setTimeout(() => c.abort(), 4500);
        const r = await fetch(`${host}/streams/${ytId}`, { signal: c.signal });
        clearTimeout(t);
        if (!r.ok) continue;
        const json = await r.json();
        const muxed = (json.videoStreams || []).find((s: { url?: string; videoOnly?: boolean }) => s.url && !s.videoOnly);
        const audio = (json.audioStreams || []).find((s: { url?: string; mimeType?: string }) => s.url && (s.mimeType || '').includes('mp4'))
          || (json.audioStreams || []).find((s: { url?: string }) => Boolean(s.url));
        if (muxed?.url || audio?.url) {
          return {
            ok: true,
            videoUrl: muxed?.url,
            audioUrl: audio?.url || muxed?.url,
            title: json.title,
            author: json.uploader,
            duration: typeof json.duration === 'number' ? json.duration : undefined,
            thumbnailUrl: json.thumbnailUrl,
          };
        }
      } catch {
        // continue
      }
    }
  }

  return { ok: false };
}

export interface ExtractedPlaylistTrack {
  url: string;
  embedUrl?: string;
  videoUrl?: string;
  audioUrl?: string;
  title: string;
  artist?: string;
  duration?: number;
  thumbnailUrl?: string;
  type: MediaType;
  sourceLabel: string;
}

export interface ExtractedPlaylistResult {
  ok: boolean;
  title?: string;
  author?: string;
  thumbnailUrl?: string;
  embedUrl?: string;
  sourceLabel?: string;
  items: ExtractedPlaylistTrack[];
}

export async function extractPlaylistFromUrl(
  rawUrl: string,
  preferredType?: MediaType
): Promise<ExtractedPlaylistResult> {
  const normalizedUrl = normalizeMediaUrl(rawUrl);
  if (!normalizedUrl) return { ok: false, items: [] };

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 12000);
  try {
    const typeParam = preferredType ? `&type=${encodeURIComponent(preferredType)}` : '';
    const res = await fetch(
      `/api/extract-playlist?url=${encodeURIComponent(normalizedUrl)}${typeParam}`,
      { signal: controller.signal }
    );
    if (res.ok) {
      const data = (await res.json()) as ExtractedPlaylistResult;
      if (data?.ok) {
        return {
          ...data,
          items: Array.isArray(data.items) ? data.items : [],
        };
      }
    }
  } catch {
    // fallback below
  } finally {
    clearTimeout(timer);
  }

  // Client-side Piped playlist fallback if backend proxy is unavailable
  const listId = extractYouTubePlaylistId(normalizedUrl);
  if (listId) {
    const isMusic = normalizedUrl.toLowerCase().includes('music.youtube.com');
    const sourceLabel = isMusic ? 'YouTube Music' : 'YouTube';
    for (const host of ['https://pipedapi.kavin.rocks', 'https://pipedapi.adminforge.de']) {
      try {
        const c = new AbortController();
        const t = setTimeout(() => c.abort(), 5500);
        const r = await fetch(`${host}/playlists/${encodeURIComponent(listId)}`, { signal: c.signal });
        clearTimeout(t);
        if (!r.ok) continue;
        const json = await r.json();
        const tracks: ExtractedPlaylistTrack[] = [];
        for (const s of (json.relatedStreams || []).slice(0, 50)) {
          const vid = s.url ? extractYouTubeVideoId(`https://www.youtube.com${s.url}`) : null;
          if (!vid) continue;
          tracks.push({
            url: `https://www.youtube.com/watch?v=${vid}`,
            embedUrl: `https://www.youtube.com/embed/${vid}?autoplay=1&rel=0&playsinline=1`,
            title: s.title || `${sourceLabel} (${vid})`,
            artist: s.uploaderName || json.uploader || sourceLabel,
            duration: typeof s.duration === 'number' ? s.duration : 0,
            thumbnailUrl: s.thumbnail || `https://img.youtube.com/vi/${vid}/hqdefault.jpg`,
            type: preferredType || (isMusic ? 'song' : 'video'),
            sourceLabel,
          });
        }
        if (tracks.length > 0) {
          return {
            ok: true,
            title: json.name || `${sourceLabel} Playlist`,
            author: json.uploader || sourceLabel,
            thumbnailUrl: json.thumbnailUrl || tracks[0].thumbnailUrl,
            sourceLabel: isMusic ? 'YouTube Music Playlist' : 'YouTube Playlist',
            items: tracks,
          };
        }
      } catch {
        // try next mirror
      }
    }
  }

  return { ok: false, items: [] };
}
