import { defineConfig, type Plugin } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import type { IncomingMessage, ServerResponse } from 'node:http';
import { Readable } from 'node:stream';
import { lookup } from 'node:dns/promises';

function isPrivateIp(ip: string): boolean {
  const clean = ip.replace(/^::ffff:/i, '').toLowerCase();
  if (clean === '::1' || clean === 'localhost' || clean === '0.0.0.0') return true;
  if (clean.startsWith('fc') || clean.startsWith('fd') || clean.startsWith('fe80:')) return true;
  const parts = clean.split('.').map((n) => Number(n));
  if (parts.length !== 4 || parts.some((n) => Number.isNaN(n) || n < 0 || n > 255)) return false;
  if (parts[0] === 10 || parts[0] === 127 || parts[0] === 0) return true;
  if (parts[0] === 169 && parts[1] === 254) return true;
  if (parts[0] === 172 && parts[1] >= 16 && parts[1] <= 31) return true;
  if (parts[0] === 192 && parts[1] === 168) return true;
  if (parts[0] === 100 && parts[1] >= 64 && parts[1] <= 127) return true;
  return false;
}

async function assertSafePublicUrl(rawUrl: string): Promise<URL> {
  const parsed = new URL(rawUrl);
  if (parsed.protocol !== 'http:' && parsed.protocol !== 'https:') {
    throw new Error('Only http/https protocols are supported');
  }
  const host = parsed.hostname.toLowerCase();
  if (host === 'localhost' || host.endsWith('.local') || isPrivateIp(host)) {
    throw new Error('Private or local network hosts are not allowed');
  }
  try {
    const resolved = await lookup(host);
    if (resolved?.address && isPrivateIp(resolved.address)) {
      throw new Error('Target resolves to a private network address');
    }
  } catch (err) {
    if (err instanceof Error && err.message.includes('private network')) {
      throw err;
    }
  }
  return parsed;
}

function extractYouTubeId(rawUrl: string): string | null {
  const match = rawUrl.match(
    /(?:youtube\.com\/watch\?(?:.*&)?v=|youtube\.com\/embed\/|youtube\.com\/v\/|youtube\.com\/shorts\/|youtube\.com\/live\/|music\.youtube\.com\/watch\?(?:.*&)?v=|youtu\.be\/)([A-Za-z0-9_-]{11})(?:[?&#/]|$)/i
  );
  return match ? match[1] : null;
}

function extractVimeoId(rawUrl: string): string | null {
  const match = rawUrl.match(
    /vimeo\.com\/(?:channels\/[^/]+\/|groups\/[^/]+\/videos\/|video\/)?(\d{5,})(?:[?#/]|$)/i
  );
  return match ? match[1] : null;
}

function extractYouTubePlaylistId(rawUrl: string): string | null {
  try {
    const u = new URL(rawUrl);
    const host = u.hostname.toLowerCase();
    if (
      host === 'youtube.com' ||
      host.endsWith('.youtube.com') ||
      host === 'youtu.be'
    ) {
      const listId = u.searchParams.get('list');
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

function toProxyStreamUrl(url: string): string {
  return `/api/stream?url=${encodeURIComponent(url)}`;
}

async function extractYouTubeStreams(videoId: string) {
  const innerTubeClients = [
    {
      userAgent:
        'com.google.android.apps.youtube.vr.oculus/1.60.19 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip',
      client: {
        clientName: 'ANDROID_VR',
        clientVersion: '1.60.19',
        deviceMake: 'Oculus',
        deviceModel: 'Quest 3',
        androidSdkVersion: 32,
        osName: 'Android',
        osVersion: '12L',
        hl: 'en',
        gl: 'US',
      },
      source: 'youtube-vr',
    },
    {
      userAgent: 'com.google.android.youtube/19.29.37 (Linux; U; Android 11) gzip',
      client: {
        clientName: 'ANDROID',
        clientVersion: '19.29.37',
        androidSdkVersion: 30,
        hl: 'en',
        gl: 'US',
      },
      source: 'youtube-android',
    },
  ];

  // 1. Try YouTube InnerTube clients (ANDROID_VR yields direct MP4 + M4A streams without YouTube UI)
  for (const cfg of innerTubeClients) {
    try {
      const res = await fetch('https://www.youtube.com/youtubei/v1/player?prettyPrint=false', {
        method: 'POST',
        signal: AbortSignal.timeout(6500),
        headers: {
          'Content-Type': 'application/json',
          'User-Agent': cfg.userAgent,
        },
        body: JSON.stringify({
          videoId,
          context: {
            client: cfg.client,
          },
        }),
      });

      if (!res.ok) continue;
      const data = (await res.json()) as {
        streamingData?: {
          formats?: Array<{ url?: string; mimeType?: string; height?: number }>;
          adaptiveFormats?: Array<{ url?: string; mimeType?: string; bitrate?: number }>;
          hlsManifestUrl?: string;
        };
        videoDetails?: {
          title?: string;
          author?: string;
          lengthSeconds?: string;
        };
      };

      const formats = data.streamingData?.formats ?? [];
      const adaptive = data.streamingData?.adaptiveFormats ?? [];

      const mp4Muxed = formats
        .filter((f) => Boolean(f.url) && (f.mimeType || '').toLowerCase().includes('mp4'))
        .sort((a, b) => (b.height || 0) - (a.height || 0));
      const anyMuxed = formats.filter((f) => Boolean(f.url));
      const bestVideo = mp4Muxed[0]?.url || anyMuxed[0]?.url;

      const audioList = adaptive
        .filter((f) => Boolean(f.url) && (f.mimeType || '').toLowerCase().startsWith('audio/'))
        .sort((a, b) => {
          const aMp4 = (a.mimeType || '').toLowerCase().includes('mp4') ? 1 : 0;
          const bMp4 = (b.mimeType || '').toLowerCase().includes('mp4') ? 1 : 0;
          if (aMp4 !== bMp4) return bMp4 - aMp4;
          return (b.bitrate || 0) - (a.bitrate || 0);
        });
      const bestAudio = audioList[0]?.url || bestVideo;

      if (bestVideo || bestAudio) {
        return {
          ok: true,
          videoUrl: bestVideo ? toProxyStreamUrl(bestVideo) : undefined,
          audioUrl: bestAudio ? toProxyStreamUrl(bestAudio) : undefined,
          title: data.videoDetails?.title,
          author: data.videoDetails?.author,
          duration: data.videoDetails?.lengthSeconds
            ? Number(data.videoDetails.lengthSeconds)
            : undefined,
          thumbnailUrl: `https://img.youtube.com/vi/${videoId}/hqdefault.jpg`,
          source: cfg.source,
        };
      }
    } catch {
      // try next client or mirror
    }
  }

  // 2. Fallback to Piped instances
  const pipedHosts = [
    'https://pipedapi.kavin.rocks',
    'https://pipedapi.adminforge.de',
    'https://pipedapi.in.projectsegfau.lt',
  ];
  for (const host of pipedHosts) {
    try {
      const res = await fetch(`${host}/streams/${videoId}`, {
        signal: AbortSignal.timeout(5500),
        headers: { Accept: 'application/json' },
      });
      if (!res.ok) continue;
      const data = (await res.json()) as {
        title?: string;
        uploader?: string;
        duration?: number;
        thumbnailUrl?: string;
        videoStreams?: Array<{ url?: string; videoOnly?: boolean; mimeType?: string; height?: number }>;
        audioStreams?: Array<{ url?: string; mimeType?: string; bitrate?: number }>;
      };
      const muxed = (data.videoStreams || []).find((s) => s.url && !s.videoOnly);
      const audio =
        (data.audioStreams || []).find(
          (s) => s.url && (s.mimeType || '').toLowerCase().includes('mp4')
        ) || (data.audioStreams || []).find((s) => Boolean(s.url));

      if (muxed?.url || audio?.url) {
        return {
          ok: true,
          videoUrl: muxed?.url ? toProxyStreamUrl(muxed.url) : undefined,
          audioUrl: audio?.url
            ? toProxyStreamUrl(audio.url)
            : muxed?.url
            ? toProxyStreamUrl(muxed.url)
            : undefined,
          title: data.title,
          author: data.uploader,
          duration: typeof data.duration === 'number' ? data.duration : undefined,
          thumbnailUrl:
            data.thumbnailUrl || `https://img.youtube.com/vi/${videoId}/hqdefault.jpg`,
          source: 'piped',
        };
      }
    } catch {
      // try next mirror
    }
  }

  // 3. Fallback to Invidious instances
  const invHosts = ['https://inv.nadeko.net', 'https://invidious.nerdvpn.de'];
  for (const host of invHosts) {
    try {
      const res = await fetch(`${host}/api/v1/videos/${videoId}`, {
        signal: AbortSignal.timeout(5500),
        headers: { Accept: 'application/json' },
      });
      if (!res.ok) continue;
      const data = (await res.json()) as {
        title?: string;
        author?: string;
        lengthSeconds?: number;
        formatStreams?: Array<{ url?: string; type?: string }>;
        adaptiveFormats?: Array<{ url?: string; type?: string }>;
      };
      const muxed =
        (data.formatStreams || []).find(
          (s) => s.url && (s.type || '').toLowerCase().includes('mp4')
        ) || (data.formatStreams || []).find((s) => Boolean(s.url));
      const audio =
        (data.adaptiveFormats || []).find(
          (s) => s.url && (s.type || '').toLowerCase().startsWith('audio/mp4')
        ) ||
        (data.adaptiveFormats || []).find(
          (s) => s.url && (s.type || '').toLowerCase().startsWith('audio/')
        );

      if (muxed?.url || audio?.url) {
        return {
          ok: true,
          videoUrl: muxed?.url ? toProxyStreamUrl(muxed.url) : undefined,
          audioUrl: audio?.url
            ? toProxyStreamUrl(audio.url)
            : muxed?.url
            ? toProxyStreamUrl(muxed.url)
            : undefined,
          title: data.title,
          author: data.author,
          duration: typeof data.lengthSeconds === 'number' ? data.lengthSeconds : undefined,
          thumbnailUrl: `https://img.youtube.com/vi/${videoId}/hqdefault.jpg`,
          source: 'invidious',
        };
      }
    } catch {
      // try next mirror
    }
  }

  return null;
}

async function extractVimeoStreams(vimeoId: string) {
  try {
    const res = await fetch(`https://player.vimeo.com/video/${vimeoId}/config`, {
      signal: AbortSignal.timeout(5500),
      headers: {
        Accept: 'application/json',
        'User-Agent': 'Mozilla/5.0',
      },
    });
    if (!res.ok) return null;
    const data = (await res.json()) as {
      request?: {
        files?: {
          progressive?: Array<{ url?: string; width?: number }>;
        };
      };
      video?: {
        title?: string;
        duration?: number;
        owner?: { name?: string };
        thumbs?: { base?: string };
      };
    };
    const progressive = (data.request?.files?.progressive || [])
      .filter((p) => Boolean(p.url))
      .sort((a, b) => (b.width || 0) - (a.width || 0));
    const best = progressive[0]?.url;
    if (!best) return null;
    return {
      ok: true,
      videoUrl: toProxyStreamUrl(best),
      audioUrl: toProxyStreamUrl(best),
      title: data.video?.title,
      author: data.video?.owner?.name,
      duration: data.video?.duration,
      thumbnailUrl: data.video?.thumbs?.base,
      source: 'vimeo',
    };
  } catch {
    return null;
  }
}

async function extractGenericPageMedia(targetUrl: URL) {
  try {
    const res = await fetch(targetUrl.toString(), {
      signal: AbortSignal.timeout(6000),
      redirect: 'follow',
      headers: {
        'User-Agent':
          'Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36',
      },
    });
    if (!res.ok) return null;
    const contentType = (res.headers.get('content-type') || '').toLowerCase();
    if (contentType.startsWith('video/') || contentType.startsWith('audio/')) {
      const proxied = toProxyStreamUrl(res.url || targetUrl.toString());
      return {
        ok: true,
        videoUrl: contentType.startsWith('video/') ? proxied : undefined,
        audioUrl: contentType.startsWith('audio/') ? proxied : proxied,
        source: 'direct',
      };
    }
    if (!contentType.includes('text/html') && !contentType.includes('application/xhtml')) {
      return null;
    }

    const html = (await res.text()).slice(0, 350_000);
    const pickMeta = (prop: string) => {
      const re = new RegExp(
        `<meta[^>]+(?:property|name)=["']${prop}["'][^>]+content=["']([^"']+)["']`,
        'i'
      );
      return html.match(re)?.[1]?.replace(/&amp;/g, '&');
    };

    const ogVideo =
      pickMeta('og:video:secure_url') ||
      pickMeta('og:video:url') ||
      pickMeta('og:video') ||
      pickMeta('twitter:player:stream');
    const ogAudio = pickMeta('og:audio:secure_url') || pickMeta('og:audio');
    const tagMatch = html.match(/<(?:video|audio|source)[^>]+src=["']([^"']+)["']/i)?.[1];
    const title = pickMeta('og:title') || html.match(/<title[^>]*>([^<]+)<\/title>/i)?.[1]?.trim();
    const thumb = pickMeta('og:image');

    const baseUrl = res.url || targetUrl.toString();
    const resolveAbs = (raw?: string) => {
      if (!raw) return undefined;
      try {
        return new URL(raw.replace(/&amp;/g, '&'), baseUrl).toString();
      } catch {
        return undefined;
      }
    };

    const rawVideo = resolveAbs(ogVideo || tagMatch);
    const rawAudio = resolveAbs(ogAudio || tagMatch);
    const isMediaHref = (u?: string) =>
      Boolean(u && !/\.(html?|php|asp|jsp)(\?|$)/i.test(u));

    const validVideo = isMediaHref(rawVideo) ? rawVideo : undefined;
    const validAudio = isMediaHref(rawAudio) ? rawAudio : undefined;

    if (!validVideo && !validAudio) return null;

    return {
      ok: true,
      videoUrl: validVideo ? toProxyStreamUrl(validVideo) : undefined,
      audioUrl: validAudio
        ? toProxyStreamUrl(validAudio)
        : validVideo
        ? toProxyStreamUrl(validVideo)
        : undefined,
      title,
      thumbnailUrl: resolveAbs(thumb),
      source: 'html-meta',
    };
  } catch {
    return null;
  }
}

interface ExtractedPlaylistTrackPayload {
  url: string;
  embedUrl?: string;
  videoUrl?: string;
  audioUrl?: string;
  title: string;
  artist?: string;
  duration?: number;
  thumbnailUrl?: string;
  type: 'video' | 'song' | 'short';
  sourceLabel: string;
}

function parseYouTubeDurationText(text?: string): number {
  if (!text) return 0;
  const parts = text.split(':').map((p) => Number(p.trim()));
  if (parts.some((n) => Number.isNaN(n))) return 0;
  if (parts.length === 3) return parts[0] * 3600 + parts[1] * 60 + parts[2];
  if (parts.length === 2) return parts[0] * 60 + parts[1];
  if (parts.length === 1) return parts[0];
  return 0;
}

async function extractYouTubePlaylist(
  playlistId: string,
  isYtMusic: boolean,
  preferredType?: 'video' | 'song' | 'short'
) {
  const defaultTrackType: 'video' | 'song' | 'short' =
    preferredType || (isYtMusic ? 'song' : 'video');
  const sourceLabel = isYtMusic ? 'YouTube Music' : 'YouTube';

  // 1. Try Piped playlist API
  const pipedHosts = [
    'https://pipedapi.kavin.rocks',
    'https://pipedapi.adminforge.de',
    'https://pipedapi.in.projectsegfau.lt',
  ];
  for (const host of pipedHosts) {
    try {
      const res = await fetch(`${host}/playlists/${encodeURIComponent(playlistId)}`, {
        signal: AbortSignal.timeout(6500),
        headers: { Accept: 'application/json' },
      });
      if (!res.ok) continue;
      const data = (await res.json()) as {
        name?: string;
        uploader?: string;
        thumbnailUrl?: string;
        relatedStreams?: Array<{
          url?: string;
          title?: string;
          uploaderName?: string;
          duration?: number;
          thumbnail?: string;
          isShort?: boolean;
        }>;
      };
      const streams = (data.relatedStreams || []).slice(0, 50);
      const items: ExtractedPlaylistTrackPayload[] = [];
      for (const s of streams) {
        const vid = s.url ? extractYouTubeId(`https://www.youtube.com${s.url}`) : null;
        if (!vid) continue;
        items.push({
          url: `https://www.youtube.com/watch?v=${vid}`,
          embedUrl: `https://www.youtube-nocookie.com/embed/${vid}?enablejsapi=1&autoplay=1&controls=0&rel=0&modestbranding=1&iv_load_policy=3&playsinline=1&fs=0&disablekb=1`,
          title: s.title || `${sourceLabel} (${vid})`,
          artist: s.uploaderName || data.uploader || sourceLabel,
          duration: typeof s.duration === 'number' && s.duration > 0 ? s.duration : 0,
          thumbnailUrl: s.thumbnail || `https://img.youtube.com/vi/${vid}/hqdefault.jpg`,
          type: s.isShort && !preferredType ? 'short' : defaultTrackType,
          sourceLabel,
        });
      }
      if (items.length > 0) {
        return {
          ok: true,
          title: data.name || `${sourceLabel} Playlist`,
          author: data.uploader || sourceLabel,
          thumbnailUrl: data.thumbnailUrl || items[0].thumbnailUrl,
          sourceLabel: isYtMusic ? 'YouTube Music Playlist' : 'YouTube Playlist',
          items,
        };
      }
    } catch {
      // try next host
    }
  }

  // 2. Try Invidious playlist API
  const invHosts = ['https://inv.nadeko.net', 'https://invidious.nerdvpn.de'];
  for (const host of invHosts) {
    try {
      const res = await fetch(`${host}/api/v1/playlists/${encodeURIComponent(playlistId)}`, {
        signal: AbortSignal.timeout(6500),
        headers: { Accept: 'application/json' },
      });
      if (!res.ok) continue;
      const data = (await res.json()) as {
        title?: string;
        author?: string;
        playlistThumbnail?: string;
        videos?: Array<{
          videoId?: string;
          title?: string;
          author?: string;
          lengthSeconds?: number;
          videoThumbnails?: Array<{ url?: string }>;
        }>;
      };
      const items: ExtractedPlaylistTrackPayload[] = (data.videos || [])
        .filter((v) => Boolean(v.videoId))
        .slice(0, 50)
        .map((v) => ({
          url: `https://www.youtube.com/watch?v=${v.videoId}`,
          embedUrl: `https://www.youtube-nocookie.com/embed/${v.videoId}?enablejsapi=1&autoplay=1&controls=0&rel=0&modestbranding=1&iv_load_policy=3&playsinline=1&fs=0&disablekb=1`,
          title: v.title || `${sourceLabel} (${v.videoId})`,
          artist: v.author || data.author || sourceLabel,
          duration: typeof v.lengthSeconds === 'number' && v.lengthSeconds > 0 ? v.lengthSeconds : 0,
          thumbnailUrl:
            v.videoThumbnails?.[0]?.url || `https://img.youtube.com/vi/${v.videoId}/hqdefault.jpg`,
          type: defaultTrackType,
          sourceLabel,
        }));
      if (items.length > 0) {
        return {
          ok: true,
          title: data.title || `${sourceLabel} Playlist`,
          author: data.author || sourceLabel,
          thumbnailUrl: data.playlistThumbnail || items[0].thumbnailUrl,
          sourceLabel: isYtMusic ? 'YouTube Music Playlist' : 'YouTube Playlist',
          items,
        };
      }
    } catch {
      // try next host
    }
  }

  // 3. Direct YouTube playlist HTML / ytInitialData scrape fallback
  try {
    const res = await fetch(`https://www.youtube.com/playlist?list=${encodeURIComponent(playlistId)}`, {
      signal: AbortSignal.timeout(7000),
      headers: {
        'User-Agent':
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36',
        'Accept-Language': 'en-US,en;q=0.9',
      },
    });
    if (res.ok) {
      const html = await res.text();
      const titleMatch = html.match(/<meta\s+property="og:title"\s+content="([^"]+)"/i)?.[1];
      const thumbMatch = html.match(/<meta\s+property="og:image"\s+content="([^"]+)"/i)?.[1];
      const dataMatch = html.match(/var\s+ytInitialData\s*=\s*(\{.*?\});\s*<\/script>/s);
      const items: ExtractedPlaylistTrackPayload[] = [];
      const seen = new Set<string>();

      if (dataMatch?.[1]) {
        try {
          const initialData = JSON.parse(dataMatch[1]) as unknown;
          const stack: unknown[] = [initialData];
          while (stack.length > 0 && items.length < 50) {
            const cur = stack.pop();
            if (!cur || typeof cur !== 'object') continue;
            if (Array.isArray(cur)) {
              for (let i = cur.length - 1; i >= 0; i--) stack.push(cur[i]);
              continue;
            }
            const obj = cur as Record<string, unknown>;
            const renderer = obj.playlistVideoRenderer as
              | {
                  videoId?: string;
                  title?: { runs?: Array<{ text?: string }>; simpleText?: string };
                  shortBylineText?: { runs?: Array<{ text?: string }> };
                  lengthText?: { simpleText?: string };
                  lengthSeconds?: string;
                }
              | undefined;
            if (renderer?.videoId && !seen.has(renderer.videoId)) {
              seen.add(renderer.videoId);
              const t =
                renderer.title?.runs?.map((r) => r.text || '').join('') ||
                renderer.title?.simpleText ||
                `${sourceLabel} (${renderer.videoId})`;
              const artist =
                renderer.shortBylineText?.runs?.map((r) => r.text || '').join('') || sourceLabel;
              const dur = renderer.lengthSeconds
                ? Number(renderer.lengthSeconds)
                : parseYouTubeDurationText(renderer.lengthText?.simpleText);
              items.push({
                url: `https://www.youtube.com/watch?v=${renderer.videoId}`,
                embedUrl: `https://www.youtube-nocookie.com/embed/${renderer.videoId}?enablejsapi=1&autoplay=1&controls=0&rel=0&modestbranding=1&iv_load_policy=3&playsinline=1&fs=0&disablekb=1`,
                title: t,
                artist,
                duration: Number.isFinite(dur) ? dur : 0,
                thumbnailUrl: `https://img.youtube.com/vi/${renderer.videoId}/hqdefault.jpg`,
                type: defaultTrackType,
                sourceLabel,
              });
              continue;
            }
            for (const v of Object.values(obj)) {
              if (v && typeof v === 'object') stack.push(v);
            }
          }
        } catch {
          // fallback regex below
        }
      }

      if (items.length > 0) {
        return {
          ok: true,
          title: titleMatch || `${sourceLabel} Playlist`,
          author: sourceLabel,
          thumbnailUrl: thumbMatch || items[0].thumbnailUrl,
          sourceLabel: isYtMusic ? 'YouTube Music Playlist' : 'YouTube Playlist',
          items,
        };
      }
    }
  } catch {
    // ignore
  }

  return null;
}

async function extractM3UPlaylist(targetUrl: URL, preferredType?: 'video' | 'song' | 'short') {
  try {
    const res = await fetch(targetUrl.toString(), {
      signal: AbortSignal.timeout(6500),
      redirect: 'follow',
      headers: { 'User-Agent': 'Mozilla/5.0' },
    });
    if (!res.ok) return null;
    const text = (await res.text()).slice(0, 600_000);
    // Distinguish single HLS stream manifests from M3U track playlists
    if (text.includes('#EXT-X-TARGETDURATION') || text.includes('#EXT-X-STREAM-INF')) {
      return null;
    }

    const lines = text
      .split(/\r?\n/)
      .map((l) => l.trim())
      .filter(Boolean);
    const items: ExtractedPlaylistTrackPayload[] = [];
    let pendingTitle = '';
    let pendingDuration = 0;

    for (const line of lines) {
      if (line.startsWith('#EXTINF:')) {
        const commaIdx = line.indexOf(',');
        const metaPart = commaIdx >= 0 ? line.slice(8, commaIdx) : line.slice(8);
        const durNum = Number(metaPart.split(/\s+/)[0]);
        pendingDuration = Number.isFinite(durNum) && durNum > 0 ? Math.round(durNum) : 0;
        pendingTitle = commaIdx >= 0 ? line.slice(commaIdx + 1).trim() : '';
        continue;
      }
      if (line.startsWith('#')) continue;

      let resolved: string;
      try {
        resolved = new URL(line, res.url || targetUrl.toString()).toString();
      } catch {
        continue;
      }
      if (!/^https?:/i.test(resolved)) continue;

      const lower = resolved.toLowerCase().split('?')[0];
      const isAudio = /\.(mp3|wav|ogg|m4a|flac|aac|opus|wma)$/i.test(lower);
      const trackType: 'video' | 'song' | 'short' =
        preferredType || (isAudio ? 'song' : 'video');
      const fallbackName =
        decodeURIComponent(new URL(resolved).pathname.split('/').pop() || '')
          .replace(/\.[^/.]+$/, '')
          .replace(/[-_]+/g, ' ')
          .trim() || `Track ${items.length + 1}`;
      const proxied = toProxyStreamUrl(resolved);

      items.push({
        url: resolved,
        videoUrl: trackType === 'song' ? undefined : proxied,
        audioUrl: proxied,
        title: pendingTitle || fallbackName,
        artist: targetUrl.hostname.replace(/^www\./, ''),
        duration: pendingDuration,
        type: trackType,
        sourceLabel: 'M3U Playlist',
      });
      pendingTitle = '';
      pendingDuration = 0;
      if (items.length >= 60) break;
    }

    if (items.length === 0) return null;
    const plName =
      decodeURIComponent(targetUrl.pathname.split('/').pop() || '')
        .replace(/\.m3u8?$/i, '')
        .replace(/[-_]+/g, ' ')
        .trim() || 'Imported M3U Playlist';

    return {
      ok: true,
      title: plName,
      author: targetUrl.hostname.replace(/^www\./, ''),
      sourceLabel: 'M3U Playlist',
      items,
    };
  } catch {
    return null;
  }
}

async function extractSpotifyPlaylist(targetUrl: URL, preferredType?: 'video' | 'song' | 'short') {
  try {
    const cleanUrl = targetUrl.toString();
    const oembedPromise = fetch(
      `https://open.spotify.com/oembed?url=${encodeURIComponent(cleanUrl)}`,
      { signal: AbortSignal.timeout(5000) }
    )
      .then((r) => (r.ok ? r.json() : null))
      .catch(() => null) as Promise<{ title?: string; thumbnail_url?: string } | null>;

    const embedUrl = cleanUrl.includes('/embed/')
      ? cleanUrl
      : cleanUrl.replace('open.spotify.com/', 'open.spotify.com/embed/');
    const embedHtmlPromise = fetch(embedUrl, {
      signal: AbortSignal.timeout(6000),
      headers: { 'User-Agent': 'Mozilla/5.0' },
    })
      .then((r) => (r.ok ? r.text() : ''))
      .catch(() => '');

    const [oembed, embedHtml] = await Promise.all([oembedPromise, embedHtmlPromise]);
    const items: ExtractedPlaylistTrackPayload[] = [];
    let title = oembed?.title || 'Spotify Playlist';
    let thumb = oembed?.thumbnail_url;

    const nextDataMatch = embedHtml.match(
      /<script[^>]+id="__NEXT_DATA__"[^>]*>([\s\S]*?)<\/script>/i
    );
    if (nextDataMatch?.[1]) {
      try {
        const nextJson = JSON.parse(nextDataMatch[1]) as {
          props?: {
            pageProps?: {
              state?: {
                data?: {
                  entity?: {
                    name?: string;
                    coverArt?: { sources?: Array<{ url?: string }> };
                    trackList?: Array<{
                      uri?: string;
                      title?: string;
                      subtitle?: string;
                      duration?: number;
                      audioPreview?: { url?: string };
                    }>;
                  };
                };
              };
            };
          };
        };
        const entity = nextJson.props?.pageProps?.state?.data?.entity;
        if (entity?.name) title = entity.name;
        if (!thumb && entity?.coverArt?.sources?.[0]?.url) {
          thumb = entity.coverArt.sources[0].url;
        }
        for (const t of (entity?.trackList || []).slice(0, 50)) {
          const trackId = t.uri?.split(':').pop();
          if (!trackId) continue;
          items.push({
            url: `https://open.spotify.com/track/${trackId}`,
            embedUrl: `https://open.spotify.com/embed/track/${trackId}`,
            audioUrl: t.audioPreview?.url ? toProxyStreamUrl(t.audioPreview.url) : undefined,
            title: t.title || `Spotify Track (${trackId})`,
            artist: t.subtitle || 'Spotify',
            duration: typeof t.duration === 'number' ? Math.round(t.duration / 1000) : 0,
            thumbnailUrl: thumb,
            type: preferredType || 'song',
            sourceLabel: 'Spotify',
          });
        }
      } catch {
        // ignore JSON parse issues
      }
    }

    if (items.length === 0 && !oembed) return null;
    return {
      ok: true,
      title,
      author: 'Spotify',
      thumbnailUrl: thumb,
      embedUrl,
      sourceLabel: 'Spotify Playlist',
      items,
    };
  } catch {
    return null;
  }
}

function sendJson(res: ServerResponse, status: number, body: unknown) {
  res.statusCode = status;
  res.setHeader('Content-Type', 'application/json; charset=utf-8');
  res.setHeader('Cache-Control', 'no-store');
  res.end(JSON.stringify(body));
}

function attachMediaMiddlewares(
  middlewares: { use: (fn: (req: IncomingMessage, res: ServerResponse, next: () => void) => void) => void }
) {
  middlewares.use(async (req, res, next) => {
    const rawReqUrl = req.url || '';
    if (
      !rawReqUrl.startsWith('/api/extract-playlist') &&
      !rawReqUrl.startsWith('/api/extract') &&
      !rawReqUrl.startsWith('/api/stream')
    ) {
      next();
      return;
    }

    const reqUrl = new URL(rawReqUrl, 'http://127.0.0.1:3000');

    if (reqUrl.pathname === '/api/extract-playlist') {
      const target = reqUrl.searchParams.get('url') || '';
      const prefRaw = reqUrl.searchParams.get('type') || undefined;
      const prefType =
        prefRaw === 'video' || prefRaw === 'song' || prefRaw === 'short' ? prefRaw : undefined;

      if (!target) {
        sendJson(res, 400, { ok: false, reason: 'missing_url' });
        return;
      }
      try {
        const safeUrl = await assertSafePublicUrl(target);
        const urlStr = safeUrl.toString();
        const ytPlaylistId = extractYouTubePlaylistId(urlStr);
        if (ytPlaylistId) {
          const isMusic = safeUrl.hostname.toLowerCase().includes('music.youtube.com');
          const pl = await extractYouTubePlaylist(ytPlaylistId, isMusic, prefType);
          sendJson(res, 200, pl || { ok: false, reason: 'playlist_unavailable' });
          return;
        }
        if (safeUrl.hostname.toLowerCase() === 'open.spotify.com') {
          const sp = await extractSpotifyPlaylist(safeUrl, prefType);
          sendJson(res, 200, sp || { ok: false, reason: 'playlist_unavailable' });
          return;
        }
        const m3u = await extractM3UPlaylist(safeUrl, prefType);
        if (m3u) {
          sendJson(res, 200, m3u);
          return;
        }
        sendJson(res, 200, { ok: false, reason: 'playlist_unavailable' });
      } catch (err) {
        sendJson(res, 400, {
          ok: false,
          reason: err instanceof Error ? err.message : 'invalid_url',
        });
      }
      return;
    }

    if (reqUrl.pathname === '/api/extract') {
      const target = reqUrl.searchParams.get('url') || '';
      if (!target) {
        sendJson(res, 400, { ok: false, reason: 'missing_url' });
        return;
      }
      try {
        const safeUrl = await assertSafePublicUrl(target);
        const ytId = extractYouTubeId(safeUrl.toString());
        if (ytId) {
          const extracted = await extractYouTubeStreams(ytId);
          sendJson(res, 200, extracted || { ok: false, reason: 'no_direct_stream' });
          return;
        }
        const vimeoId = extractVimeoId(safeUrl.toString());
        if (vimeoId) {
          const extracted = await extractVimeoStreams(vimeoId);
          sendJson(res, 200, extracted || { ok: false, reason: 'no_direct_stream' });
          return;
        }
        const generic = await extractGenericPageMedia(safeUrl);
        sendJson(res, 200, generic || { ok: false, reason: 'no_direct_stream' });
      } catch (err) {
        sendJson(res, 400, {
          ok: false,
          reason: err instanceof Error ? err.message : 'invalid_url',
        });
      }
      return;
    }

    if (reqUrl.pathname === '/api/stream') {
      const target = reqUrl.searchParams.get('url') || '';
      if (!target) {
        res.statusCode = 400;
        res.end('Missing url');
        return;
      }
      const controller = new AbortController();
      req.on('close', () => controller.abort());
      try {
        const safeUrl = await assertSafePublicUrl(target);
        const headers: Record<string, string> = {
          'User-Agent':
            'Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36',
        };
        if (typeof req.headers.range === 'string') {
          headers.Range = req.headers.range;
        }

        const upstream = await fetch(safeUrl.toString(), {
          headers,
          redirect: 'follow',
          signal: controller.signal,
        });

        res.statusCode = upstream.status;
        res.setHeader('Access-Control-Allow-Origin', '*');
        const forwardHeaders = [
          'content-type',
          'content-length',
          'content-range',
          'accept-ranges',
          'cache-control',
        ];
        for (const h of forwardHeaders) {
          const val = upstream.headers.get(h);
          if (val) res.setHeader(h, val);
        }
        if (!upstream.headers.get('accept-ranges')) {
          res.setHeader('Accept-Ranges', 'bytes');
        }

        if (!upstream.body) {
          res.end();
          return;
        }
        const nodeStream = Readable.fromWeb(
          upstream.body as unknown as import('node:stream/web').ReadableStream
        );
        nodeStream.on('error', () => {
          if (!res.writableEnded) res.end();
        });
        nodeStream.pipe(res);
      } catch {
        if (!res.headersSent) {
          res.statusCode = 502;
          res.end('Upstream media fetch failed');
        }
      }
      return;
    }

    next();
  });
}

function v2sMediaExtractorPlugin(): Plugin {
  return {
    name: 'v2s-media-extractor-proxy',
    configureServer(server) {
      attachMediaMiddlewares(server.middlewares);
    },
    configurePreviewServer(server) {
      attachMediaMiddlewares(server.middlewares);
    },
  };
}

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss(), v2sMediaExtractorPlugin()],
  server: {
    port: 3000,
    host: '0.0.0.0',
    strictPort: true,
  },
  preview: {
    port: 3000,
    host: '0.0.0.0',
    strictPort: true,
  },
});
