import React, { useRef, useState, useEffect } from 'react';
import {
  Play, Pause, Volume2, VolumeX, Maximize2, Minimize2, RotateCcw,
  RotateCw, PictureInPicture, Camera, Info, X,
  Repeat, Sliders, ChevronDown, AlertTriangle, RefreshCw, Pencil, Trash2, Loader2, Headphones
} from 'lucide-react';
import { MediaItem } from '../types/media';
import {
  describeMediaPlaybackError,
  extractPlayableMedia,
  fetchOEmbedMetadata,
  getCleanEmbedUrl,
} from '../utils/mediaParser';
import { CleanYouTubeController, CleanYouTubePlayer } from './CleanYouTubePlayer';

interface VideoPlayerModalProps {
  item: MediaItem | null;
  resumeTimeSec?: number;
  onClose: () => void;
  onOpenEqualizer: () => void;
  onEditItem?: (item: MediaItem) => void;
  onDeleteItem?: (id: string) => void;
  onPlayInBackground?: (item: MediaItem, resumeTimeSec: number) => void;
  onPlaybackSync?: (item: MediaItem, isPlaying: boolean, currentTime: number, duration: number) => void;
}

export const VideoPlayerModal: React.FC<VideoPlayerModalProps> = ({
  item,
  resumeTimeSec = 0,
  onClose,
  onOpenEqualizer,
  onEditItem,
  onDeleteItem,
  onPlayInBackground,
  onPlaybackSync,
}) => {
  const videoRef = useRef<HTMLVideoElement>(null);
  const embedCtrlRef = useRef<CleanYouTubeController | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [volume, setVolume] = useState(1);
  const [isMuted, setIsMuted] = useState(false);
  const [playbackRate, setPlaybackRate] = useState(1);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [aspectRatio, setAspectRatio] = useState<'contain' | 'cover' | '16/9' | '4/3'>('contain');
  const [showControls, setShowControls] = useState(true);
  const [showInfo, setShowInfo] = useState(false);
  const [showSpeedMenu, setShowSpeedMenu] = useState(false);
  const [isLooping, setIsLooping] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // Stream extraction & error recovery states
  const [resolvedStreamUrl, setResolvedStreamUrl] = useState<string | undefined>(undefined);
  const [forceEmbedFallback, setForceEmbedFallback] = useState(false);
  const [isExtracting, setIsExtracting] = useState(false);
  const [isLoadingMedia, setIsLoadingMedia] = useState(false);
  const [mediaError, setMediaError] = useState<string | null>(null);
  const [bypassOEmbedBlock, setBypassOEmbedBlock] = useState(false);
  const [retrySeed, setRetrySeed] = useState(0);

  const hideTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const cleanEmbedUrl = item ? getCleanEmbedUrl(item) : undefined;
  const directSrc = forceEmbedFallback
    ? undefined
    : resolvedStreamUrl || item?.extractedVideoUrl || (!cleanEmbedUrl ? item?.uri : undefined);
  const isEmbed = Boolean(cleanEmbedUrl && !directSrc);

  useEffect(() => {
    if (!item) return;
    setMediaError(null);
    setForceEmbedFallback(false);
    setBypassOEmbedBlock(false);
    setCurrentTime(resumeTimeSec || 0);
    setDuration(item.duration || 0);
    setResolvedStreamUrl(item.extractedVideoUrl);

    let cancelled = false;

    const needsOnDemandExtract = Boolean(
      item.sourceType === 'link' && !item.extractedVideoUrl && item.embedUrl
    );

    if (needsOnDemandExtract) {
      setIsExtracting(true);
      setIsLoadingMedia(true);
      extractPlayableMedia(item.uri).then((extracted) => {
        if (cancelled) return;
        setIsExtracting(false);
        if (extracted.ok && (extracted.videoUrl || extracted.audioUrl)) {
          setResolvedStreamUrl(extracted.videoUrl || extracted.audioUrl);
        } else {
          // Fall back to embedded player and verify link via oEmbed
          fetchOEmbedMetadata(item.uri).then((meta) => {
            if (cancelled) return;
            if (meta.status === 'not_found') {
              setIsLoadingMedia(false);
              setMediaError(
                'Could not fetch this video (404 Not Found, deleted, or wrong video link).'
              );
            }
          });
        }
      });
    } else if (item.embedUrl && !item.extractedVideoUrl) {
      setIsLoadingMedia(true);
      fetchOEmbedMetadata(item.uri).then((meta) => {
        if (cancelled) return;
        if (meta.status === 'not_found') {
          setIsLoadingMedia(false);
          setMediaError(
            'Could not fetch this video (404 Not Found, deleted, or wrong video link).'
          );
        }
      });
    }

    const safetyTimer = setTimeout(() => {
      if (!cancelled) setIsLoadingMedia(false);
    }, 5500);

    return () => {
      cancelled = true;
      clearTimeout(safetyTimer);
    };
  }, [item?.id, item?.uri, item?.extractedVideoUrl, retrySeed]);

  useEffect(() => {
    if (!item || !directSrc || isExtracting) return;
    setIsPlaying(true);
    setIsLoadingMedia(true);
    if (videoRef.current) {
      videoRef.current.load();
      if (resumeTimeSec > 0) {
        const onReadySeek = () => {
          if (videoRef.current) {
            try {
              videoRef.current.currentTime = resumeTimeSec;
            } catch {
              // ignore seek before metadata
            }
            videoRef.current.removeEventListener('loadedmetadata', onReadySeek);
          }
        };
        videoRef.current.addEventListener('loadedmetadata', onReadySeek);
      }
      videoRef.current.play().catch(() => setIsPlaying(false));
    }
  }, [directSrc, isExtracting, retrySeed, item?.id]);

  // Sync MediaSession controls for video playback too (top notification + lockscreen)
  useEffect(() => {
    if (!item) return;
    onPlaybackSync?.(item, isPlaying, currentTime, duration);
  }, [item, isPlaying, currentTime, duration, onPlaybackSync]);

  useEffect(() => {
    if (!item || typeof navigator === 'undefined' || !('mediaSession' in navigator)) return;
    try {
      navigator.mediaSession.setActionHandler('play', () => {
        if (isEmbed) embedCtrlRef.current?.play();
        else videoRef.current?.play().catch(() => {});
        setIsPlaying(true);
      });
      navigator.mediaSession.setActionHandler('pause', () => {
        if (isEmbed) embedCtrlRef.current?.pause();
        else videoRef.current?.pause();
        setIsPlaying(false);
      });
      navigator.mediaSession.setActionHandler('seekbackward', (details) => {
        seekBy(-(details.seekOffset || 10));
      });
      navigator.mediaSession.setActionHandler('seekforward', (details) => {
        seekBy(details.seekOffset || 10);
      });
    } catch {
      // ignore unsupported handlers
    }
  }, [item?.id, isEmbed]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (!item) return;
      if (e.code === 'Escape') {
        if (!isFullscreen) onClose();
        return;
      }
      if (mediaError) return;
      if (e.code === 'Space') {
        e.preventDefault();
        togglePlay();
      } else if (e.code === 'ArrowRight') {
        seekBy(10);
      } else if (e.code === 'ArrowLeft') {
        seekBy(-10);
      } else if (e.code === 'KeyF') {
        toggleFullscreen();
      } else if (e.code === 'KeyM') {
        toggleMute();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [item, isFullscreen, isPlaying, isMuted, volume, isEmbed, mediaError, currentTime, duration]);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 2500);
  };

  const resetHideTimer = () => {
    setShowControls(true);
    if (hideTimeoutRef.current) clearTimeout(hideTimeoutRef.current);
    hideTimeoutRef.current = setTimeout(() => {
      if (isPlaying && !mediaError) setShowControls(false);
    }, 3500);
  };

  if (!item) return null;

  const handleRetry = () => {
    setMediaError(null);
    setForceEmbedFallback(false);
    setBypassOEmbedBlock(true);
    setIsLoadingMedia(true);
    setRetrySeed((prev) => prev + 1);
  };

  const togglePlay = () => {
    if (mediaError) return;
    if (isEmbed) {
      if (isPlaying) {
        embedCtrlRef.current?.pause();
        setIsPlaying(false);
      } else {
        embedCtrlRef.current?.play();
        setIsPlaying(true);
      }
      resetHideTimer();
      return;
    }
    if (!videoRef.current) return;
    if (videoRef.current.paused) {
      videoRef.current.play().catch(() => {});
      setIsPlaying(true);
    } else {
      videoRef.current.pause();
      setIsPlaying(false);
    }
    resetHideTimer();
  };

  const seekBy = (sec: number) => {
    if (isEmbed) {
      const target = Math.max(0, Math.min(duration || currentTime + 600, currentTime + sec));
      embedCtrlRef.current?.seekTo(target);
      setCurrentTime(target);
      showToast(sec > 0 ? `+${sec}s` : `${sec}s`);
      resetHideTimer();
      return;
    }
    if (!videoRef.current) return;
    videoRef.current.currentTime = Math.max(
      0,
      Math.min(videoRef.current.duration || 0, videoRef.current.currentTime + sec)
    );
    showToast(sec > 0 ? `+${sec}s` : `${sec}s`);
    resetHideTimer();
  };

  const handleSeekChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const time = parseFloat(e.target.value);
    setCurrentTime(time);
    if (isEmbed) {
      embedCtrlRef.current?.seekTo(time);
      resetHideTimer();
      return;
    }
    if (!videoRef.current) return;
    videoRef.current.currentTime = time;
    resetHideTimer();
  };

  const handleVolumeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = parseFloat(e.target.value);
    setVolume(val);
    setIsMuted(val === 0);
    if (isEmbed) {
      embedCtrlRef.current?.setVolume(val);
      embedCtrlRef.current?.setMuted(val === 0);
    } else if (videoRef.current) {
      videoRef.current.volume = val;
      videoRef.current.muted = val === 0;
    }
    resetHideTimer();
  };

  const toggleMute = () => {
    const nextMuted = !isMuted;
    setIsMuted(nextMuted);
    if (isEmbed) {
      embedCtrlRef.current?.setMuted(nextMuted);
    } else if (videoRef.current) {
      videoRef.current.muted = nextMuted;
    }
    resetHideTimer();
  };

  const handleSpeedSelect = (rate: number) => {
    setPlaybackRate(rate);
    if (isEmbed) {
      embedCtrlRef.current?.setPlaybackRate(rate);
    } else if (videoRef.current) {
      videoRef.current.playbackRate = rate;
    }
    setShowSpeedMenu(false);
    showToast(`Speed: ${rate}x`);
  };

  const toggleFullscreen = () => {
    if (!containerRef.current) return;
    if (!document.fullscreenElement) {
      containerRef.current.requestFullscreen?.();
      setIsFullscreen(true);
    } else {
      document.exitFullscreen?.();
      setIsFullscreen(false);
    }
  };

  const togglePiP = async () => {
    if (!videoRef.current) return;
    try {
      if (document.pictureInPictureElement) {
        await document.exitPictureInPicture();
      } else if (videoRef.current.requestPictureInPicture) {
        await videoRef.current.requestPictureInPicture();
      }
    } catch {
      showToast('Picture-in-Picture not supported in this view');
    }
  };

  const captureSnapshot = () => {
    if (!videoRef.current) return;
    const video = videoRef.current;
    const canvas = document.createElement('canvas');
    canvas.width = video.videoWidth || 1920;
    canvas.height = video.videoHeight || 1080;
    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
      const link = document.createElement('a');
      link.download = `V2S-Screenshot-${item.title.replace(/\s+/g, '_')}-${Math.floor(currentTime)}s.png`;
      link.href = canvas.toDataURL('image/png');
      link.click();
      showToast('Screenshot saved');
    }
  };

  const formatTime = (seconds: number) => {
    if (isNaN(seconds)) return '0:00';
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  };

  const showErrorOverlay = Boolean(mediaError && !(isEmbed && bypassOEmbedBlock));

  return (
    <div
      ref={containerRef}
      onMouseMove={resetHideTimer}
      onClick={resetHideTimer}
      className="fixed inset-0 z-50 bg-black flex flex-col items-center justify-center select-none overflow-hidden v2s-animate-fade"
    >
      {toastMessage && (
        <div className="absolute top-16 z-30 px-4 py-2 bg-slate-900/90 text-white border border-slate-700/80 rounded-xl text-xs font-semibold backdrop-blur-md shadow-2xl v2s-animate-toast">
          {toastMessage}
        </div>
      )}

      {/* Top Bar Controls */}
      <div
        className={`${
          showErrorOverlay
            ? 'relative w-full bg-slate-950 border-b border-slate-800'
            : 'absolute top-0 inset-x-0 bg-gradient-to-b from-black/85 via-black/45 to-transparent'
        } px-3 sm:px-5 py-3 flex items-center justify-between gap-2 sm:gap-3 z-30 transition-opacity duration-200 ${
          showControls || showErrorOverlay ? 'opacity-100' : 'opacity-0 pointer-events-none'
        }`}
      >
        <div className="flex items-center gap-2.5 sm:gap-3 min-w-0">
          <button
            type="button"
            onClick={onClose}
            className="min-h-[40px] min-w-[40px] flex items-center justify-center text-white/85 hover:text-white rounded-full bg-white/10 hover:bg-white/20 transition-colors shrink-0"
            title="Close Player (Esc)"
          >
            <X className="w-5 h-5" />
          </button>
          <div className="min-w-0">
            <h3 className="text-white font-semibold text-xs sm:text-base truncate max-w-[180px] sm:max-w-md">
              {item.title}
            </h3>
            <p className="text-[11px] sm:text-xs text-slate-400 flex items-center gap-1.5 truncate">
              <span>{item.artist || item.sourceLabel || 'Local Media'}</span>
              <span aria-hidden="true">&middot;</span>
              <span className="text-indigo-400 font-mono text-[11px]">
                {directSrc ? 'In-App Stream' : item.sourceLabel || 'Media'}
              </span>
            </p>
          </div>
        </div>

        <div className="flex items-center gap-1.5 sm:gap-2 shrink-0">
          {!showErrorOverlay && onPlayInBackground && (
            <button
              type="button"
              onClick={() => onPlayInBackground(item, currentTime)}
              className="min-h-[38px] px-2.5 py-1.5 text-xs text-indigo-200 hover:text-white rounded-lg bg-indigo-600/35 hover:bg-indigo-600/55 border border-indigo-400/35 flex items-center gap-1.5 transition-colors whitespace-nowrap"
              title="Play in Background Audio Deck"
            >
              <Headphones className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">Background</span>
            </button>
          )}

          {item.sourceType === 'link' && onEditItem && (
            <button
              type="button"
              onClick={() => onEditItem(item)}
              className="min-h-[38px] px-2.5 py-1.5 text-xs text-slate-200 hover:text-white rounded-lg bg-white/10 hover:bg-white/20 hidden sm:flex items-center gap-1.5 transition-colors whitespace-nowrap"
              title="Edit / Fix Link"
            >
              <Pencil className="w-3.5 h-3.5" />
              <span>Edit Link</span>
            </button>
          )}

          {!isEmbed && !showErrorOverlay && !isExtracting && (
            <>
              <button
                type="button"
                onClick={onOpenEqualizer}
                className="min-h-[38px] px-2.5 py-1.5 text-white/85 hover:text-indigo-400 rounded-lg bg-white/10 hover:bg-white/20 transition-colors text-xs hidden sm:flex items-center gap-1.5"
                title="Open V2S Equalizer"
              >
                <Sliders className="w-4 h-4" />
                <span>EQ</span>
              </button>

              <button
                type="button"
                onClick={captureSnapshot}
                className="min-h-[38px] min-w-[38px] hidden md:flex items-center justify-center text-white/85 hover:text-white rounded-full bg-white/10 hover:bg-white/20 transition-colors"
                title="Capture Screenshot"
              >
                <Camera className="w-4 h-4" />
              </button>

              <button
                type="button"
                onClick={() => {
                  const modes: ('contain' | 'cover' | '16/9' | '4/3')[] = ['contain', 'cover', '16/9', '4/3'];
                  const next = modes[(modes.indexOf(aspectRatio) + 1) % modes.length];
                  setAspectRatio(next);
                  showToast(`Aspect: ${next.toUpperCase()}`);
                }}
                className="min-h-[38px] px-2 py-1 text-[11px] text-white/85 hover:text-white rounded-lg bg-white/10 hover:bg-white/20 font-mono hidden sm:flex items-center"
                title="Cycle Aspect Ratio"
              >
                {aspectRatio.toUpperCase()}
              </button>
            </>
          )}

          <button
            type="button"
            onClick={toggleFullscreen}
            className="min-h-[38px] min-w-[38px] flex items-center justify-center text-white/85 hover:text-white rounded-full bg-white/10 hover:bg-white/20 transition-colors"
            title="Toggle Fullscreen"
          >
            {isFullscreen ? <Minimize2 className="w-4 h-4" /> : <Maximize2 className="w-4 h-4" />}
          </button>

          <button
            type="button"
            onClick={() => setShowInfo(!showInfo)}
            className="min-h-[38px] min-w-[38px] flex items-center justify-center text-white/85 hover:text-white rounded-full bg-white/10 hover:bg-white/20 transition-colors"
            title="Media Details"
          >
            <Info className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Info Popover */}
      {showInfo && (
        <div className="absolute top-16 right-4 z-30 p-4 bg-slate-900/95 border border-slate-800 rounded-xl text-xs text-slate-300 w-72 max-w-[calc(100vw-2rem)] backdrop-blur-md shadow-2xl space-y-2 v2s-animate-modal">
          <div className="flex justify-between items-center pb-2 border-b border-slate-800">
            <span className="font-bold text-white">Media Information</span>
            <button type="button" onClick={() => setShowInfo(false)} className="text-slate-500 hover:text-white">
              <X className="w-4 h-4" />
            </button>
          </div>
          <div className="space-y-1 font-mono text-[11px]">
            <div className="flex justify-between">
              <span className="text-slate-500">Source:</span>
              <span className="text-emerald-400">{item.sourceLabel || item.folder}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">Playback:</span>
              <span>{directSrc ? 'In-App Direct Stream' : 'Embedded Player'}</span>
            </div>
          </div>
        </div>
      )}

      {/* Error Recovery Screen */}
      {showErrorOverlay ? (
        <div className="flex-1 w-full flex items-center justify-center p-6">
          <div className="w-full max-w-md rounded-2xl bg-slate-900 border border-slate-800 p-6 text-center space-y-4 shadow-2xl v2s-animate-modal">
            <div className="w-12 h-12 rounded-2xl bg-red-500/15 text-red-400 flex items-center justify-center mx-auto">
              <AlertTriangle className="w-6 h-6" />
            </div>
            <div className="space-y-1.5">
              <h4 className="text-base font-bold text-white">Unable to Fetch Video</h4>
              <p className="text-xs text-slate-300 leading-relaxed">{mediaError}</p>
            </div>
            <p className="text-[11px] text-slate-500 font-mono break-all bg-slate-950 border border-slate-800 rounded-xl px-3 py-2">
              {item.uri}
            </p>
            <div className="flex flex-wrap items-center justify-center gap-2 pt-1">
              <button
                type="button"
                onClick={handleRetry}
                className="min-h-[40px] px-3.5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold flex items-center gap-1.5 transition-colors"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                {isEmbed ? 'Try Player Anyway' : 'Retry'}
              </button>

              {item.sourceType === 'link' && onEditItem && (
                <button
                  type="button"
                  onClick={() => onEditItem(item)}
                  className="min-h-[40px] px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-white text-xs font-semibold flex items-center gap-1.5 transition-colors"
                >
                  <Pencil className="w-3.5 h-3.5" />
                  Edit / Fix Link
                </button>
              )}

              {onDeleteItem && (
                <button
                  type="button"
                  onClick={() => {
                    onDeleteItem(item.id);
                    onClose();
                  }}
                  className="min-h-[40px] px-3.5 py-2 rounded-xl bg-red-500/15 hover:bg-red-500/25 text-red-300 text-xs font-semibold flex items-center gap-1.5 transition-colors"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                  Remove
                </button>
              )}
            </div>
          </div>
        </div>
      ) : isExtracting ? (
        <div className="flex-1 flex flex-col items-center justify-center gap-3 text-slate-300">
          <Loader2 className="w-9 h-9 text-indigo-400 animate-spin" />
          <span className="text-xs sm:text-sm font-medium">Loading video player...</span>
        </div>
      ) : (
        <div className="relative w-full h-full flex items-center justify-center bg-black">
          {isEmbed && cleanEmbedUrl ? (
            <div className="w-full h-full max-w-6xl max-h-full aspect-video flex items-center justify-center">
              <CleanYouTubePlayer
                key={`${item.id}-${retrySeed}`}
                embedUrl={cleanEmbedUrl}
                title={item.title}
                thumbnail={item.thumbnail}
                mode="video"
                loop={isLooping}
                volume={volume}
                isMuted={isMuted}
                playbackRate={playbackRate}
                controllerRef={embedCtrlRef}
                onSurfaceClick={togglePlay}
                onSurfaceDoubleClick={toggleFullscreen}
                onPlayStateChange={(playing) => setIsPlaying(playing)}
                onProgress={(ct, dur) => {
                  setCurrentTime(ct);
                  setDuration(dur);
                }}
                onEnded={() => setIsPlaying(false)}
                onReady={() => setIsLoadingMedia(false)}
                onError={() => {
                  setIsLoadingMedia(false);
                  setMediaError('Failed to load video stream from this link.');
                }}
              />
            </div>
          ) : (
            <>
              <video
                key={`${item.id}-${directSrc}-${retrySeed}`}
                ref={videoRef}
                src={directSrc}
                loop={isLooping}
                playsInline
                autoPlay
                onLoadStart={() => setIsLoadingMedia(true)}
                onWaiting={() => setIsLoadingMedia(true)}
                onCanPlay={() => setIsLoadingMedia(false)}
                onPlay={() => setIsPlaying(true)}
                onPause={() => setIsPlaying(false)}
                onLoadedMetadata={() => {
                  setIsLoadingMedia(false);
                  if (videoRef.current) setDuration(videoRef.current.duration);
                }}
                onTimeUpdate={() => videoRef.current && setCurrentTime(videoRef.current.currentTime)}
                onEnded={() => setIsPlaying(false)}
                onError={() => {
                  if (cleanEmbedUrl) {
                    setForceEmbedFallback(true);
                    setResolvedStreamUrl(undefined);
                    setMediaError(null);
                    return;
                  }
                  setIsLoadingMedia(false);
                  setIsPlaying(false);
                  setMediaError(describeMediaPlaybackError(videoRef.current?.error?.code, item));
                }}
                onClick={togglePlay}
                className={`w-full h-full transition-all duration-300 ${
                  aspectRatio === 'cover'
                    ? 'object-cover'
                    : aspectRatio === '16/9'
                    ? 'aspect-video object-contain'
                    : aspectRatio === '4/3'
                    ? 'aspect-[4/3] object-contain'
                    : 'object-contain'
                }`}
              />

              {isLoadingMedia && (
                <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                  <div className="p-4 rounded-2xl bg-black/75 backdrop-blur-sm flex items-center gap-2 text-xs text-slate-200">
                    <Loader2 className="w-5 h-5 text-indigo-400 animate-spin" />
                    <span>Buffering video...</span>
                  </div>
                </div>
              )}

              {!isLoadingMedia && !isPlaying && showControls && (
                <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                  <div className="p-5 bg-indigo-600/85 rounded-full text-white backdrop-blur-sm shadow-2xl">
                    <Play className="w-10 h-10 fill-current translate-x-0.5" />
                  </div>
                </div>
              )}
            </>
          )}

          {/* Bottom Bar Controls */}
          <div
            className={`absolute bottom-0 inset-x-0 p-3 sm:p-6 bg-gradient-to-t from-black/95 via-black/70 to-transparent z-30 space-y-2 transition-opacity duration-200 ${
              showControls ? 'opacity-100' : 'opacity-0 pointer-events-none'
            }`}
          >
            <div className="flex items-center gap-2 sm:gap-3">
              <span className="text-[11px] sm:text-xs text-white/90 font-mono w-10 text-right tabular-nums">
                {formatTime(currentTime)}
              </span>
              <div className="relative flex-1 group py-2 cursor-pointer">
                <input
                  type="range"
                  min="0"
                  max={duration || 100}
                  step="0.1"
                  value={currentTime}
                  onChange={handleSeekChange}
                  className="v2s-slider w-full h-1.5 bg-white/25 rounded-lg appearance-none cursor-pointer accent-indigo-500"
                />
              </div>
              <span className="text-[11px] sm:text-xs text-slate-400 font-mono w-10 tabular-nums">
                {formatTime(duration)}
              </span>
            </div>

            <div className="flex items-center justify-between gap-2">
              <div className="flex items-center gap-1.5 sm:gap-3">
                <button
                  type="button"
                  onClick={togglePlay}
                  className="min-h-[40px] min-w-[40px] flex items-center justify-center text-white hover:text-indigo-400 rounded-full bg-white/10 hover:bg-white/20 transition-colors"
                >
                  {isPlaying ? (
                    <Pause className="w-5 h-5 fill-current" />
                  ) : (
                    <Play className="w-5 h-5 fill-current translate-x-0.5" />
                  )}
                </button>

                <button
                  type="button"
                  onClick={() => seekBy(-10)}
                  className="min-h-[38px] min-w-[38px] flex items-center justify-center text-white/85 hover:text-white rounded-full hover:bg-white/10 transition-colors"
                  title="Seek 10s backward"
                >
                  <RotateCcw className="w-4 h-4" />
                </button>

                <button
                  type="button"
                  onClick={() => seekBy(10)}
                  className="min-h-[38px] min-w-[38px] flex items-center justify-center text-white/85 hover:text-white rounded-full hover:bg-white/10 transition-colors"
                  title="Seek 10s forward"
                >
                  <RotateCw className="w-4 h-4" />
                </button>

                <div className="flex items-center gap-1.5 group">
                  <button
                    type="button"
                    onClick={toggleMute}
                    className="min-h-[38px] min-w-[38px] flex items-center justify-center text-white/85 hover:text-white rounded-full hover:bg-white/10"
                  >
                    {isMuted || volume === 0 ? (
                      <VolumeX className="w-4 h-4 text-red-400" />
                    ) : (
                      <Volume2 className="w-4 h-4" />
                    )}
                  </button>
                  <input
                    type="range"
                    min="0"
                    max="1"
                    step="0.05"
                    value={isMuted ? 0 : volume}
                    onChange={handleVolumeChange}
                    className="v2s-slider hidden sm:block w-20 md:w-24 h-1 bg-white/25 rounded appearance-none cursor-pointer accent-indigo-500"
                  />
                </div>
              </div>

              <div className="flex items-center gap-1.5 sm:gap-2.5">
                <button
                  type="button"
                  onClick={() => {
                    setIsLooping(!isLooping);
                    showToast(isLooping ? 'Repeat: Off' : 'Repeat: On');
                  }}
                  className={`min-h-[38px] min-w-[38px] flex items-center justify-center rounded-full transition-colors ${
                    isLooping
                      ? 'bg-indigo-600 text-white'
                      : 'text-white/80 hover:text-white hover:bg-white/10'
                  }`}
                  title="Toggle Repeat"
                >
                  <Repeat className="w-4 h-4" />
                </button>

                <div className="relative">
                  <button
                    type="button"
                    onClick={() => setShowSpeedMenu(!showSpeedMenu)}
                    className="min-h-[36px] px-2.5 py-1 text-xs font-mono rounded-lg bg-white/10 hover:bg-white/20 text-white flex items-center gap-1"
                  >
                    <span>{playbackRate}x</span>
                    <ChevronDown className="w-3 h-3" />
                  </button>

                  {showSpeedMenu && (
                    <div className="absolute bottom-11 right-0 py-1 bg-slate-900 border border-slate-800 rounded-xl shadow-2xl text-xs text-white z-30 min-w-24">
                      {[0.5, 0.75, 1.0, 1.25, 1.5, 2.0].map((rate) => (
                        <button
                          key={rate}
                          type="button"
                          onClick={() => handleSpeedSelect(rate)}
                          className={`w-full px-3 py-1.5 text-left hover:bg-indigo-600 font-mono ${
                            playbackRate === rate ? 'text-indigo-400 font-bold bg-white/5' : ''
                          }`}
                        >
                          {rate}x
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                {!isEmbed && (
                  <button
                    type="button"
                    onClick={togglePiP}
                    className="min-h-[38px] min-w-[38px] text-white/85 hover:text-white rounded-full hover:bg-white/10 transition-colors hidden sm:flex items-center justify-center"
                    title="Picture in Picture"
                  >
                    <PictureInPicture className="w-4 h-4" />
                  </button>
                )}

                <button
                  type="button"
                  onClick={toggleFullscreen}
                  className="min-h-[38px] min-w-[38px] flex items-center justify-center text-white hover:text-indigo-400 rounded-full hover:bg-white/10 transition-colors"
                  title="Toggle Fullscreen (F)"
                >
                  {isFullscreen ? (
                    <Minimize2 className="w-5 h-5" />
                  ) : (
                    <Maximize2 className="w-5 h-5" />
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
