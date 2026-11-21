import React, { useRef, useState, useEffect } from 'react';
import {
  Heart, MessageCircle, Share2, Volume2, VolumeX,
  Play, ChevronUp, ChevronDown, Music, Disc3,
  Bookmark, X, Send, Link2,
  AlertTriangle, RefreshCw, Pencil, Trash2, Loader2
} from 'lucide-react';
import { MediaItem } from '../types/media';
import {
  describeMediaPlaybackError,
  extractPlayableMedia,
  fetchOEmbedMetadata,
  getCleanEmbedUrl,
} from '../utils/mediaParser';
import { CleanYouTubePlayer } from './CleanYouTubePlayer';

interface ShortsPlayerViewProps {
  shorts: MediaItem[];
  selectedShortId?: string | null;
  onOpenSong?: (songId: string) => void;
  onAddLink?: () => void;
  onEditItem?: (item: MediaItem) => void;
  onDeleteItem?: (id: string) => void;
}

interface ShortComment {
  id: string;
  user: string;
  text: string;
  time: string;
}

const COMMENTS_STORAGE_KEY = 'v2s_shorts_comments_v1';

export const ShortsPlayerView: React.FC<ShortsPlayerViewProps> = ({
  shorts,
  selectedShortId,
  onAddLink,
  onEditItem,
  onDeleteItem,
}) => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(true);
  const [isMuted, setIsMuted] = useState(true);
  const [likedMap, setLikedMap] = useState<Record<string, boolean>>({});
  const [likesCountMap, setLikesCountMap] = useState<Record<string, number>>({});
  const [savedMap, setSavedMap] = useState<Record<string, boolean>>({});
  const [showHeartPop, setShowHeartPop] = useState(false);
  const [showComments, setShowComments] = useState(false);
  const [commentsByShort, setCommentsByShort] = useState<Record<string, ShortComment[]>>(() => {
    try {
      const saved = localStorage.getItem(COMMENTS_STORAGE_KEY);
      return saved ? JSON.parse(saved) : {};
    } catch {
      return {};
    }
  });
  const [newComment, setNewComment] = useState('');
  const [toastMsg, setToastMsg] = useState<string | null>(null);

  const [resolvedShortUrl, setResolvedShortUrl] = useState<string | undefined>(undefined);
  const [isExtracting, setIsExtracting] = useState(false);
  const [isLoadingShort, setIsLoadingShort] = useState(false);
  const [shortError, setShortError] = useState<string | null>(null);
  const [bypassOEmbedBlock, setBypassOEmbedBlock] = useState(false);
  const [retrySeed, setRetrySeed] = useState(0);

  const videoRef = useRef<HTMLVideoElement>(null);
  const touchStartYRef = useRef<number | null>(null);
  const safeIndex = shorts.length > 0 ? Math.min(currentIndex, shorts.length - 1) : 0;
  const activeShort = shorts[safeIndex];

  const cleanEmbedUrl = activeShort ? getCleanEmbedUrl(activeShort) : undefined;
  const directSrc =
    resolvedShortUrl ||
    activeShort?.extractedVideoUrl ||
    (!cleanEmbedUrl ? activeShort?.uri : undefined);
  const isEmbed = Boolean(cleanEmbedUrl && !directSrc);

  useEffect(() => {
    if (!selectedShortId) return;
    const idx = shorts.findIndex((s) => s.id === selectedShortId);
    if (idx >= 0) setCurrentIndex(idx);
  }, [selectedShortId, shorts]);

  useEffect(() => {
    const initialCounts: Record<string, number> = {};
    shorts.forEach((s) => {
      initialCounts[s.id] = s.likes || 0;
    });
    setLikesCountMap(initialCounts);
  }, [shorts]);

  useEffect(() => {
    try {
      localStorage.setItem(COMMENTS_STORAGE_KEY, JSON.stringify(commentsByShort));
    } catch {
      // ignore storage errors
    }
  }, [commentsByShort]);

  useEffect(() => {
    if (!activeShort) return;
    setShortError(null);
    setBypassOEmbedBlock(false);
    setResolvedShortUrl(activeShort.extractedVideoUrl);
    let cancelled = false;

    const needsOnDemandExtract = Boolean(
      activeShort.sourceType === 'link' && !activeShort.extractedVideoUrl && activeShort.embedUrl
    );

    if (needsOnDemandExtract) {
      setIsExtracting(true);
      setIsLoadingShort(true);
      extractPlayableMedia(activeShort.uri).then((extracted) => {
        if (cancelled) return;
        setIsExtracting(false);
        if (extracted.ok && (extracted.videoUrl || extracted.audioUrl)) {
          setResolvedShortUrl(extracted.videoUrl || extracted.audioUrl);
        } else {
          fetchOEmbedMetadata(activeShort.uri).then((meta) => {
            if (cancelled) return;
            if (meta.status === 'not_found') {
              setIsLoadingShort(false);
              setShortError('Could not fetch this Short (404 / removed or wrong video ID).');
            }
          });
        }
      });
    } else if (activeShort.embedUrl && !activeShort.extractedVideoUrl) {
      setIsLoadingShort(true);
      fetchOEmbedMetadata(activeShort.uri).then((meta) => {
        if (cancelled) return;
        if (meta.status === 'not_found') {
          setIsLoadingShort(false);
          setShortError('Could not fetch this Short (404 / removed or wrong video ID).');
        }
      });
    }

    const safetyTimer = setTimeout(() => {
      if (!cancelled) setIsLoadingShort(false);
    }, 5000);

    return () => {
      cancelled = true;
      clearTimeout(safetyTimer);
    };
  }, [safeIndex, activeShort?.id, activeShort?.uri, retrySeed]);

  useEffect(() => {
    if (!activeShort || !directSrc || isExtracting) return;
    setIsPlaying(true);
    setIsLoadingShort(true);
    if (videoRef.current) {
      videoRef.current.currentTime = 0;
      videoRef.current.load();
      videoRef.current.play().catch(() => setIsPlaying(false));
    }
  }, [directSrc, isExtracting, retrySeed, activeShort?.id]);

  // Keyboard navigation on laptop
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        handleNext();
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        handlePrev();
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  });

  const showToast = (msg: string) => {
    setToastMsg(msg);
    setTimeout(() => setToastMsg(null), 2000);
  };

  const handleNext = () => {
    if (shorts.length === 0) return;
    setCurrentIndex((prev) => (prev < shorts.length - 1 ? prev + 1 : 0));
  };

  const handlePrev = () => {
    if (shorts.length === 0) return;
    setCurrentIndex((prev) => (prev > 0 ? prev - 1 : shorts.length - 1));
  };

  const handleRetry = () => {
    setShortError(null);
    setBypassOEmbedBlock(true);
    setIsLoadingShort(true);
    setRetrySeed((prev) => prev + 1);
  };

  const togglePlay = () => {
    if (!videoRef.current || isEmbed || shortError) return;
    if (videoRef.current.paused) {
      videoRef.current.play().catch(() => {});
      setIsPlaying(true);
    } else {
      videoRef.current.pause();
      setIsPlaying(false);
    }
  };

  const toggleMute = () => {
    if (!videoRef.current || isEmbed) return;
    const nextMuted = !isMuted;
    videoRef.current.muted = nextMuted;
    setIsMuted(nextMuted);
    showToast(nextMuted ? 'Sound Muted' : 'Sound On');
  };

  const handleToggleLike = () => {
    if (!activeShort) return;
    const isLiked = !likedMap[activeShort.id];
    setLikedMap((prev) => ({ ...prev, [activeShort.id]: isLiked }));
    setLikesCountMap((prev) => ({
      ...prev,
      [activeShort.id]: Math.max(0, (prev[activeShort.id] || 0) + (isLiked ? 1 : -1)),
    }));
    if (isLiked) {
      setShowHeartPop(true);
      setTimeout(() => setShowHeartPop(false), 900);
    }
  };

  const handleDoubleTap = () => {
    if (!activeShort) return;
    if (!likedMap[activeShort.id]) {
      handleToggleLike();
    } else {
      setShowHeartPop(true);
      setTimeout(() => setShowHeartPop(false), 900);
    }
  };

  const handleShare = () => {
    if (!activeShort) return;
    navigator.clipboard?.writeText(activeShort.uri);
    showToast('Link copied to clipboard');
  };

  const handleAddComment = (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeShort || !newComment.trim()) return;
    const entry: ShortComment = {
      id: Date.now().toString(),
      user: 'You',
      text: newComment.trim(),
      time: 'Just now',
    };
    setCommentsByShort((prev) => ({
      ...prev,
      [activeShort.id]: [entry, ...(prev[activeShort.id] || [])],
    }));
    setNewComment('');
  };

  if (!activeShort) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[55vh] text-center px-6 bg-slate-900/30 rounded-3xl border border-slate-800/80 space-y-4 v2s-animate-fade">
        <div className="w-14 h-14 rounded-2xl bg-indigo-500/15 text-indigo-400 flex items-center justify-center">
          <Link2 className="w-7 h-7" />
        </div>
        <div className="space-y-1 max-w-md">
          <h3 className="text-lg font-bold text-white">No Shorts added yet</h3>
          <p className="text-xs sm:text-sm text-slate-400">
            Paste any YouTube Shorts link or load vertical videos from your device memory to watch them in the Shorts feed.
          </p>
        </div>
        {onAddLink && (
          <button
            type="button"
            onClick={onAddLink}
            className="min-h-[42px] px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs sm:text-sm font-semibold flex items-center gap-2 shadow-lg shadow-indigo-600/25 transition-colors"
          >
            <Link2 className="w-4 h-4" />
            Add Shorts Link
          </button>
        )}
      </div>
    );
  }

  const activeComments = commentsByShort[activeShort.id] || [];
  const showErrorOverlay = Boolean(shortError && !(isEmbed && bypassOEmbedBlock));

  return (
    <div className="relative flex items-center justify-center min-h-[calc(100dvh-11.5rem)] md:min-h-[calc(100dvh-9.5rem)] py-1 sm:py-2 select-none v2s-animate-fade">
      {toastMsg && (
        <div className="fixed top-20 z-50 px-4 py-2 bg-slate-900/90 text-white border border-slate-700/80 rounded-xl text-xs font-semibold backdrop-blur-md shadow-2xl v2s-animate-toast">
          {toastMsg}
        </div>
      )}

      {/* Viewport-safe 9:16 Shorts Frame for both Phone and Laptop */}
      <div
        onTouchStart={(e) => {
          touchStartYRef.current = e.touches[0]?.clientY ?? null;
        }}
        onTouchEnd={(e) => {
          const startY = touchStartYRef.current;
          const endY = e.changedTouches[0]?.clientY ?? null;
          touchStartYRef.current = null;
          if (startY === null || endY === null) return;
          const delta = startY - endY;
          if (Math.abs(delta) > 55) {
            if (delta > 0) handleNext();
            else handlePrev();
          }
        }}
        className="relative h-[min(calc(100dvh-11.5rem),760px)] md:h-[min(calc(100dvh-9.5rem),760px)] max-w-full aspect-[9/16] bg-black rounded-2xl sm:rounded-3xl overflow-hidden shadow-2xl border border-slate-800"
      >
        {showHeartPop && (
          <div className="absolute inset-0 z-30 flex items-center justify-center pointer-events-none">
            <Heart className="w-24 h-24 text-red-500 fill-red-500 drop-shadow-2xl" />
          </div>
        )}

        {showErrorOverlay ? (
          <div className="w-full h-full flex flex-col items-center justify-center p-6 text-center bg-slate-950 space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-red-500/15 text-red-400 flex items-center justify-center">
              <AlertTriangle className="w-6 h-6" />
            </div>
            <div className="space-y-1">
              <h4 className="text-sm font-bold text-white">Unable to Fetch Short</h4>
              <p className="text-xs text-slate-300 leading-relaxed">{shortError}</p>
            </div>
            <p className="w-full text-[10px] text-slate-500 font-mono break-all bg-slate-900 border border-slate-800 rounded-xl px-3 py-2">
              {activeShort.uri}
            </p>
            <div className="flex flex-wrap items-center justify-center gap-2">
              <button
                type="button"
                onClick={handleRetry}
                className="min-h-[38px] px-3 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold flex items-center gap-1.5 transition-colors"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                {isEmbed ? 'Try Anyway' : 'Retry'}
              </button>
              {activeShort.sourceType === 'link' && onEditItem && (
                <button
                  type="button"
                  onClick={() => onEditItem(activeShort)}
                  className="min-h-[38px] px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-white text-xs font-semibold flex items-center gap-1.5 transition-colors"
                >
                  <Pencil className="w-3.5 h-3.5" />
                  Edit Link
                </button>
              )}
              {shorts.length > 1 && (
                <button
                  type="button"
                  onClick={handleNext}
                  className="min-h-[38px] px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold transition-colors"
                >
                  Next Short
                </button>
              )}
              {onDeleteItem && (
                <button
                  type="button"
                  onClick={() => onDeleteItem(activeShort.id)}
                  className="min-h-[38px] px-3 py-2 rounded-xl bg-red-500/15 hover:bg-red-500/25 text-red-300 text-xs font-semibold flex items-center gap-1.5 transition-colors"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                  Remove
                </button>
              )}
            </div>
          </div>
        ) : isExtracting ? (
          <div className="w-full h-full flex flex-col items-center justify-center gap-3 text-slate-300 bg-slate-950">
            <Loader2 className="w-8 h-8 text-indigo-400 animate-spin" />
            <span className="text-xs font-medium">Loading Short in-app...</span>
          </div>
        ) : isEmbed && cleanEmbedUrl ? (
          <CleanYouTubePlayer
            key={`${activeShort.id}-${retrySeed}`}
            embedUrl={cleanEmbedUrl}
            title={activeShort.title}
            thumbnail={activeShort.thumbnail}
            mode="short"
            loop
            onReady={() => setIsLoadingShort(false)}
            onError={() => {
              setIsLoadingShort(false);
              setShortError('Could not load embedded Short.');
            }}
          />
        ) : (
          <video
            key={`${activeShort.id}-${directSrc}-${retrySeed}`}
            ref={videoRef}
            src={directSrc}
            loop
            playsInline
            autoPlay
            muted={isMuted}
            onLoadStart={() => setIsLoadingShort(true)}
            onWaiting={() => setIsLoadingShort(true)}
            onCanPlay={() => setIsLoadingShort(false)}
            onPlay={() => setIsPlaying(true)}
            onPause={() => setIsPlaying(false)}
            onError={() => {
              if (resolvedShortUrl && activeShort.embedUrl) {
                setResolvedShortUrl(undefined);
                setShortError(null);
                return;
              }
              setIsLoadingShort(false);
              setIsPlaying(false);
              setShortError(
                describeMediaPlaybackError(videoRef.current?.error?.code, activeShort)
              );
            }}
            onDoubleClick={handleDoubleTap}
            onClick={togglePlay}
            className="w-full h-full object-cover cursor-pointer"
          />
        )}

        {isLoadingShort && !showErrorOverlay && !isExtracting && (
          <div className="absolute inset-0 z-10 flex items-center justify-center pointer-events-none">
            <div className="p-3 rounded-full bg-black/65 backdrop-blur-sm">
              <Loader2 className="w-6 h-6 text-indigo-400 animate-spin" />
            </div>
          </div>
        )}

        {!isEmbed && !isPlaying && !showErrorOverlay && !isLoadingShort && !isExtracting && (
          <div
            onClick={togglePlay}
            className="absolute inset-0 z-10 flex items-center justify-center bg-black/30 cursor-pointer"
          >
            <div className="p-4 bg-indigo-600/80 backdrop-blur-sm rounded-full text-white shadow-xl">
              <Play className="w-8 h-8 fill-current translate-x-0.5" />
            </div>
          </div>
        )}

        {/* Top Header */}
        <div className="absolute top-3 sm:top-4 inset-x-3 sm:inset-x-4 z-20 flex justify-between items-center text-white/95 pointer-events-none">
          <span className="font-bold text-xs tracking-wide bg-black/60 px-3 py-1 rounded-lg backdrop-blur-md border border-white/10 pointer-events-auto tabular-nums">
            Shorts &middot; {safeIndex + 1}/{shorts.length}
          </span>

          <div className="flex items-center gap-1.5 pointer-events-auto">
            {activeShort.sourceType === 'link' && onEditItem && (
              <button
                type="button"
                onClick={() => onEditItem(activeShort)}
                className="min-h-[38px] min-w-[38px] flex items-center justify-center rounded-full bg-black/55 backdrop-blur-md hover:bg-black/75 transition-colors border border-white/10"
                title="Edit Short Link"
              >
                <Pencil className="w-4 h-4 text-white" />
              </button>
            )}
            {!isEmbed && !showErrorOverlay && (
              <button
                type="button"
                onClick={toggleMute}
                className="min-h-[38px] min-w-[38px] flex items-center justify-center rounded-full bg-black/55 backdrop-blur-md hover:bg-black/75 transition-colors border border-white/10"
              >
                {isMuted ? <VolumeX className="w-4 h-4 text-red-400" /> : <Volume2 className="w-4 h-4 text-white" />}
              </button>
            )}
            {onDeleteItem && (
              <button
                type="button"
                onClick={() => onDeleteItem(activeShort.id)}
                className="min-h-[38px] min-w-[38px] flex items-center justify-center rounded-full bg-black/55 backdrop-blur-md hover:bg-red-600/80 transition-colors border border-white/10"
                title="Remove Short"
              >
                <Trash2 className="w-4 h-4 text-white" />
              </button>
            )}
          </div>
        </div>

        {/* Right Actions */}
        {!showErrorOverlay && (
          <div className="absolute right-3 bottom-20 z-20 flex flex-col items-center gap-3.5 text-white">
            <button
              type="button"
              onClick={handleToggleLike}
              className="flex flex-col items-center gap-1 active:scale-90 transition-transform"
            >
              <div
                className={`p-2.5 rounded-full backdrop-blur-md transition-colors ${
                  likedMap[activeShort.id]
                    ? 'bg-red-500/20 text-red-500 border border-red-500/30'
                    : 'bg-black/50 hover:bg-black/70 text-white border border-white/10'
                }`}
              >
                <Heart className={`w-5 h-5 ${likedMap[activeShort.id] ? 'fill-current' : ''}`} />
              </div>
              <span className="text-[11px] font-semibold drop-shadow tabular-nums">
                {(likesCountMap[activeShort.id] || 0).toLocaleString()}
              </span>
            </button>

            <button
              type="button"
              onClick={() => setShowComments(true)}
              className="flex flex-col items-center gap-1 active:scale-90 transition-transform"
            >
              <div className="p-2.5 rounded-full bg-black/50 hover:bg-black/70 backdrop-blur-md border border-white/10">
                <MessageCircle className="w-5 h-5" />
              </div>
              <span className="text-[11px] font-semibold drop-shadow tabular-nums">
                {activeComments.length}
              </span>
            </button>

            <button
              type="button"
              onClick={() => {
                const next = !savedMap[activeShort.id];
                setSavedMap((prev) => ({ ...prev, [activeShort.id]: next }));
                showToast(next ? 'Bookmarked' : 'Removed bookmark');
              }}
              className="flex flex-col items-center gap-1 active:scale-90 transition-transform"
            >
              <div
                className={`p-2.5 rounded-full backdrop-blur-md transition-colors ${
                  savedMap[activeShort.id]
                    ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                    : 'bg-black/50 hover:bg-black/70 text-white border border-white/10'
                }`}
              >
                <Bookmark className={`w-5 h-5 ${savedMap[activeShort.id] ? 'fill-current' : ''}`} />
              </div>
              <span className="text-[11px] font-semibold drop-shadow">Save</span>
            </button>

            <button
              type="button"
              onClick={handleShare}
              className="flex flex-col items-center gap-1 active:scale-90 transition-transform"
            >
              <div className="p-2.5 rounded-full bg-black/50 hover:bg-black/70 backdrop-blur-md border border-white/10">
                <Share2 className="w-5 h-5" />
              </div>
              <span className="text-[11px] font-semibold drop-shadow">Copy</span>
            </button>

            <div className="w-9 h-9 rounded-full bg-slate-900 border-2 border-indigo-500 flex items-center justify-center">
              <Disc3 className="w-5 h-5 text-indigo-400" />
            </div>
          </div>
        )}

        {/* Bottom Caption */}
        {!showErrorOverlay && (
          <div className="absolute bottom-4 inset-x-4 z-20 space-y-1.5 text-white pointer-events-none pr-16">
            <h4 className="font-bold text-sm drop-shadow truncate">
              {activeShort.artist || activeShort.sourceLabel || 'Saved Short'}
            </h4>
            <p className="text-xs text-slate-200 line-clamp-2 drop-shadow">
              {activeShort.title}
            </p>
            <div className="flex items-center gap-1.5 text-[11px] text-indigo-300 drop-shadow">
              <Music className="w-3 h-3 shrink-0" />
              <span className="truncate font-mono">
                {activeShort.sourceLabel || activeShort.folder}
              </span>
            </div>
          </div>
        )}

        {/* Mobile Prev/Next inside edge */}
        {shorts.length > 1 && (
          <div className="lg:hidden absolute left-3 top-1/2 -translate-y-1/2 z-20 flex flex-col gap-2">
            <button
              type="button"
              onClick={handlePrev}
              className="min-h-[38px] min-w-[38px] flex items-center justify-center rounded-full bg-black/50 hover:bg-black/75 text-white border border-white/10"
              title="Previous Short"
            >
              <ChevronUp className="w-4 h-4" />
            </button>
            <button
              type="button"
              onClick={handleNext}
              className="min-h-[38px] min-w-[38px] flex items-center justify-center rounded-full bg-black/50 hover:bg-black/75 text-white border border-white/10"
              title="Next Short"
            >
              <ChevronDown className="w-4 h-4" />
            </button>
          </div>
        )}

        {/* Comments Drawer */}
        {showComments && (
          <div className="absolute inset-0 z-40 bg-slate-950/95 backdrop-blur-lg flex flex-col v2s-animate-fade">
            <div className="flex items-center justify-between p-4 border-b border-slate-800">
              <span className="font-bold text-white text-sm">
                Notes ({activeComments.length})
              </span>
              <button
                type="button"
                onClick={() => setShowComments(false)}
                className="p-1 text-slate-400 hover:text-white rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {activeComments.length === 0 ? (
                <p className="text-xs text-slate-500 text-center py-8">
                  No notes yet. Add a note for this Short below.
                </p>
              ) : (
                activeComments.map((c) => (
                  <div key={c.id} className="flex items-start gap-2.5 text-xs">
                    <div className="w-7 h-7 rounded-full bg-indigo-600/30 text-indigo-400 font-bold flex items-center justify-center shrink-0">
                      {c.user[0]}
                    </div>
                    <div className="space-y-0.5 flex-1">
                      <div className="flex items-center justify-between">
                        <span className="font-semibold text-slate-300">{c.user}</span>
                        <span className="text-[10px] text-slate-500">{c.time}</span>
                      </div>
                      <p className="text-slate-200">{c.text}</p>
                    </div>
                  </div>
                ))
              )}
            </div>

            <form onSubmit={handleAddComment} className="p-3 border-t border-slate-800 flex gap-2">
              <input
                type="text"
                placeholder="Write a personal note..."
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                className="flex-1 px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500"
              />
              <button
                type="submit"
                className="p-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl"
              >
                <Send className="w-4 h-4" />
              </button>
            </form>
          </div>
        )}
      </div>

      {/* Desktop Next / Prev Reel Buttons */}
      {shorts.length > 1 && (
        <div className="hidden lg:flex flex-col gap-3 ml-6">
          <button
            type="button"
            onClick={handlePrev}
            className="p-3 rounded-full bg-slate-800 hover:bg-slate-700 text-white shadow-lg transition-transform hover:scale-105"
            title="Previous Short (ArrowUp)"
          >
            <ChevronUp className="w-5 h-5" />
          </button>
          <button
            type="button"
            onClick={handleNext}
            className="p-3 rounded-full bg-slate-800 hover:bg-slate-700 text-white shadow-lg transition-transform hover:scale-105"
            title="Next Short (ArrowDown)"
          >
            <ChevronDown className="w-5 h-5" />
          </button>
        </div>
      )}
    </div>
  );
};
