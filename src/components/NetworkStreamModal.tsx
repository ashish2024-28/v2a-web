import React, { useEffect, useState } from 'react';
import {
  Link2, Play, X, Film, Music, Flame, AlertCircle, CheckCircle2, Save
} from 'lucide-react';
import { MediaItem, MediaType } from '../types/media';
import {
  extractPlayableMedia,
  fetchOEmbedMetadata,
  isPlaylistUrl,
  parseMediaUrl,
  validateMediaUrl,
} from '../utils/mediaParser';

interface NetworkStreamModalProps {
  isOpen: boolean;
  onClose: () => void;
  onPlayStream: (item: MediaItem) => void;
  onImportPlaylistLink?: (
    url: string,
    customTitle?: string,
    preferredType?: MediaType,
    forceOverride?: boolean
  ) => Promise<{ ok: boolean; error?: string; allowForce?: boolean }>;
  editingItem?: MediaItem | null;
  onUpdateStream?: (id: string, updated: MediaItem) => void;
  existingItems?: MediaItem[];
}

export const NetworkStreamModal: React.FC<NetworkStreamModalProps> = ({
  isOpen,
  onClose,
  onPlayStream,
  onImportPlaylistLink,
  editingItem = null,
  onUpdateStream,
  existingItems = [],
}) => {
  const [streamUrl, setStreamUrl] = useState('');
  const [streamTitle, setStreamTitle] = useState('');
  const [artistInput, setArtistInput] = useState('');
  const [streamType, setStreamType] = useState<MediaType>('video');
  const [isSaving, setIsSaving] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [warnUnverified, setWarnUnverified] = useState<string | null>(null);

  useEffect(() => {
    if (!isOpen) return;
    if (editingItem) {
      setStreamUrl(editingItem.uri);
      setStreamTitle(editingItem.title);
      setArtistInput(editingItem.artist || '');
      setStreamType(editingItem.type);
    } else {
      setStreamUrl('');
      setStreamTitle('');
      setArtistInput('');
      setStreamType('video');
    }
    setErrorMsg(null);
    setWarnUnverified(null);
    setIsSaving(false);
  }, [isOpen, editingItem]);

  if (!isOpen) return null;

  const isEditMode = Boolean(editingItem);

  const handleUrlChange = (val: string) => {
    setStreamUrl(val);
    setErrorMsg(null);
    setWarnUnverified(null);
    if (val.trim().length > 6 && !isEditMode) {
      const parsed = parseMediaUrl(val);
      setStreamType(parsed.inferredType);
    }
  };

  const finalizeSave = async (forceOverride: boolean) => {
    const validation = validateMediaUrl(streamUrl);
    if (!validation.valid) {
      setErrorMsg(validation.reason || 'Enter a valid media URL.');
      return;
    }

    const normalizedUrl = validation.normalizedUrl;

    // Playlist link import (YouTube/YT Music Playlist, Spotify Playlist/Album, SoundCloud Set, M3U)
    if (!isEditMode && isPlaylistUrl(normalizedUrl) && onImportPlaylistLink) {
      setIsSaving(true);
      setErrorMsg(null);
      setWarnUnverified(null);
      try {
        const res = await onImportPlaylistLink(
          normalizedUrl,
          streamTitle.trim() || undefined,
          streamType,
          forceOverride
        );
        if (res.ok) {
          onClose();
        } else if (res.allowForce) {
          setWarnUnverified(
            res.error || 'Could not extract tracks from this playlist link.'
          );
        } else {
          setErrorMsg(
            res.error || 'Could not fetch this playlist link. Check that the playlist is public.'
          );
        }
      } finally {
        setIsSaving(false);
      }
      return;
    }

    // Duplicate check (skip the item currently being edited)
    const duplicate = existingItems.find(
      (item) =>
        item.id !== editingItem?.id &&
        item.uri.trim().toLowerCase() === normalizedUrl.toLowerCase()
    );
    if (duplicate && !forceOverride) {
      setErrorMsg(
        `This link is already saved as "${duplicate.title}". Edit the existing item or change the URL.`
      );
      return;
    }

    setIsSaving(true);
    setErrorMsg(null);
    try {
      const parsed = parseMediaUrl(normalizedUrl, streamType);
      let resolvedTitle = streamTitle.trim();
      let resolvedArtist = artistInput.trim();
      let resolvedThumb = parsed.thumbnailUrl;

      const [meta, extracted] = await Promise.all([
        fetchOEmbedMetadata(parsed.normalizedUrl),
        extractPlayableMedia(parsed.normalizedUrl),
      ]);

      if (meta.status === 'not_found' && !extracted.ok && !forceOverride) {
        setWarnUnverified(
          'This video or track could not be found (404 / deleted or wrong video ID). Check the link for typos or click "Save Anyway".'
        );
        setIsSaving(false);
        return;
      }

      if (meta.status === 'ok') {
        if (!resolvedTitle && meta.title) resolvedTitle = meta.title;
        if (!resolvedArtist && meta.author) resolvedArtist = meta.author;
        if (!resolvedThumb && meta.thumbnailUrl) resolvedThumb = meta.thumbnailUrl;
      }
      if (extracted.ok) {
        if (!resolvedTitle && extracted.title) resolvedTitle = extracted.title;
        if (!resolvedArtist && extracted.author) resolvedArtist = extracted.author;
        if (!resolvedThumb && extracted.thumbnailUrl) resolvedThumb = extracted.thumbnailUrl;
      }

      const resolvedDuration = typeof extracted.duration === 'number' && extracted.duration > 0
        ? extracted.duration
        : (editingItem?.duration || 0);

      if (isEditMode && editingItem && onUpdateStream) {
        const updatedItem: MediaItem = {
          ...editingItem,
          title: resolvedTitle || parsed.defaultTitle,
          artist: resolvedArtist || parsed.sourceLabel,
          duration: resolvedDuration,
          uri: parsed.normalizedUrl,
          embedUrl: parsed.embedUrl,
          extractedVideoUrl: extracted.videoUrl,
          extractedAudioUrl: extracted.audioUrl,
          thumbnail: resolvedThumb || editingItem.thumbnail,
          type: streamType,
          folder: parsed.sourceLabel,
          size: extracted.videoUrl || extracted.audioUrl
            ? `${parsed.sourceLabel} (In-App)`
            : parsed.embedUrl
            ? parsed.sourceLabel
            : 'Stream URL',
          sourceLabel: parsed.sourceLabel,
        };
        onUpdateStream(editingItem.id, updatedItem);
      } else {
        const newItem: MediaItem = {
          id: `link-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
          title: resolvedTitle || parsed.defaultTitle,
          artist: resolvedArtist || parsed.sourceLabel,
          duration: resolvedDuration,
          uri: parsed.normalizedUrl,
          embedUrl: parsed.embedUrl,
          extractedVideoUrl: extracted.videoUrl,
          extractedAudioUrl: extracted.audioUrl,
          thumbnail: resolvedThumb,
          type: streamType,
          folder: parsed.sourceLabel,
          size: extracted.videoUrl || extracted.audioUrl
            ? `${parsed.sourceLabel} (In-App)`
            : parsed.embedUrl
            ? parsed.sourceLabel
            : 'Stream URL',
          sourceType: 'link',
          sourceLabel: parsed.sourceLabel,
          isFavorite: false,
          createdAt: Date.now(),
        };
        onPlayStream(newItem);
      }

      onClose();
    } finally {
      setIsSaving(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await finalizeSave(false);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md v2s-animate-fade">
      <div className="relative w-full max-w-lg bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden flex flex-col v2s-animate-modal">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/50">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-indigo-500/20 text-indigo-400 rounded-xl">
              <Link2 className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-white">
                {isEditMode ? 'Edit Media Link' : 'Add Link or Playlist'}
              </h2>
              <p className="text-xs text-slate-400">
                YouTube, Playlists, Shorts, Music, Spotify, SoundCloud, Vimeo, or MP4/MP3/M3U
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="min-h-[40px] min-w-[40px] flex items-center justify-center text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
            aria-label="Close modal"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-slate-300 tracking-wide">
              Media or Playlist URL
            </label>
            <input
              type="text"
              required
              autoFocus
              placeholder="https://youtube.com/playlist?list=... or watch?v=... or .mp4/.mp3"
              value={streamUrl}
              onChange={(e) => handleUrlChange(e.target.value)}
              className={`w-full px-3.5 py-2.5 bg-slate-950 border rounded-xl text-sm text-white placeholder-slate-600 focus:outline-none font-mono transition-colors ${
                errorMsg
                  ? 'border-red-500/80 focus:border-red-400'
                  : 'border-slate-800 focus:border-indigo-500'
              }`}
            />
            {errorMsg && (
              <div className="flex items-start gap-2 rounded-xl bg-red-500/10 border border-red-500/30 px-3 py-2 text-xs text-red-300">
                <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
                <span>{errorMsg}</span>
              </div>
            )}
            {warnUnverified && (
              <div className="rounded-xl bg-amber-500/10 border border-amber-500/30 p-3 space-y-2 text-xs text-amber-200">
                <div className="flex items-start gap-2">
                  <AlertCircle className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
                  <span>{warnUnverified}</span>
                </div>
                <div className="flex justify-end">
                  <button
                    type="button"
                    onClick={() => finalizeSave(true)}
                    disabled={isSaving}
                    className="px-3 py-1.5 rounded-lg bg-amber-500/20 hover:bg-amber-500/30 text-amber-200 font-semibold transition-colors"
                  >
                    Save Anyway
                  </button>
                </div>
              </div>
            )}
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-slate-300 tracking-wide">
                Title (Optional)
              </label>
              <input
                type="text"
                placeholder="Auto-fetched if blank"
                value={streamTitle}
                onChange={(e) => setStreamTitle(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white placeholder-slate-600 focus:outline-none focus:border-indigo-500 transition-colors"
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-slate-300 tracking-wide">
                Channel / Artist (Optional)
              </label>
              <input
                type="text"
                placeholder="e.g. YouTube, Artist Name"
                value={artistInput}
                onChange={(e) => setArtistInput(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white placeholder-slate-600 focus:outline-none focus:border-indigo-500 transition-colors"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-slate-300 tracking-wide">
              Save As
            </label>
            <div className="flex bg-slate-950 p-1 rounded-xl border border-slate-800 gap-1">
              {[
                { id: 'video' as MediaType, label: 'Video', icon: Film },
                { id: 'short' as MediaType, label: 'Short', icon: Flame },
                { id: 'song' as MediaType, label: 'Song', icon: Music },
              ].map((opt) => {
                const Icon = opt.icon;
                const active = streamType === opt.id;
                return (
                  <button
                    key={opt.id}
                    type="button"
                    onClick={() => setStreamType(opt.id)}
                    className={`flex-1 py-2 text-xs font-semibold rounded-lg flex items-center justify-center gap-1.5 transition-colors whitespace-nowrap ${
                      active
                        ? 'bg-indigo-600 text-white shadow'
                        : 'text-slate-400 hover:text-white'
                    }`}
                  >
                    <Icon className="w-3.5 h-3.5" /> {opt.label}
                  </button>
                );
              })}
            </div>
          </div>

          {streamUrl.trim().length > 4 && !errorMsg && validateMediaUrl(streamUrl).valid && (
            <div className="flex items-center gap-2 text-xs text-emerald-400">
              <CheckCircle2 className="w-3.5 h-3.5 shrink-0" />
              <span className="truncate">
                {isPlaylistUrl(streamUrl)
                  ? `Playlist detected (${parseMediaUrl(streamUrl, streamType).sourceLabel}) — imports & queues tracks`
                  : `Detected: ${parseMediaUrl(streamUrl, streamType).sourceLabel}`}
              </span>
            </div>
          )}

          <div className="flex justify-end gap-3 pt-4 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium rounded-lg text-xs transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSaving}
              className="px-5 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-60 text-white font-semibold rounded-lg text-xs flex items-center gap-1.5 shadow-lg shadow-indigo-600/30 transition-all active:scale-[0.98]"
            >
              {isEditMode ? (
                <Save className="w-3.5 h-3.5" />
              ) : (
                <Play className="w-3.5 h-3.5 fill-current" />
              )}
              {isSaving
                ? isEditMode
                  ? 'Updating...'
                  : 'Checking & Saving...'
                : isEditMode
                ? 'Update Link'
                : 'Save & Play'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
