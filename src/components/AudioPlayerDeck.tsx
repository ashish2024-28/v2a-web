import React, { useRef, useState, useEffect } from 'react';
import {
  Play, Pause, SkipBack, SkipForward, Volume2, VolumeX,
  Shuffle, Repeat, Sliders, ChevronUp, ChevronDown, ListMusic,
  Heart, Disc3, Music, X,
  AlertTriangle, RefreshCw, Pencil, Trash2
} from 'lucide-react';
import { MediaItem } from '../types/media';
import { fetchOEmbedMetadata, getCleanEmbedUrl } from '../utils/mediaParser';
import { CleanYouTubeController, CleanYouTubePlayer } from './CleanYouTubePlayer';

interface AudioPlayerDeckProps {
  currentSong: MediaItem | null;
  playlist: MediaItem[];
  isPlaying: boolean;
  audioError: string | null;
  onTogglePlay: () => void;
  onRetryAudio: () => void;
  onNext: () => void;
  onPrev: () => void;
  onSelectSong: (song: MediaItem) => void;
  onToggleFavorite: (id: string) => void;
  onEditSong?: (song: MediaItem) => void;
  onDeleteSong?: (id: string) => void;
  onOpenEqualizer: () => void;
  audioRef: React.RefObject<HTMLAudioElement | null>;
  currentGains: number[];
  bassBoost: number;
}

export const AudioPlayerDeck: React.FC<AudioPlayerDeckProps> = ({
  currentSong,
  playlist,
  isPlaying,
  audioError,
  onTogglePlay,
  onRetryAudio,
  onNext,
  onPrev,
  onSelectSong,
  onToggleFavorite,
  onEditSong,
  onDeleteSong,
  onOpenEqualizer,
  audioRef,
}) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [volume, setVolume] = useState(0.85);
  const [isMuted, setIsMuted] = useState(false);
  const [isShuffle, setIsShuffle] = useState(false);
  const [repeatMode, setRepeatMode] = useState<'off' | 'all' | 'one'>('all');
  const [showQueue, setShowQueue] = useState(false);
  const [thumbError, setThumbError] = useState(false);
  const [embedError, setEmbedError] = useState<string | null>(null);
  const [bypassEmbedBlock, setBypassEmbedBlock] = useState(false);
  const [embedRetrySeed, setEmbedRetrySeed] = useState(0);
  const [embedPlaying, setEmbedPlaying] = useState(false);
  const [embedTime, setEmbedTime] = useState(0);
  const [embedDuration, setEmbedDuration] = useState(0);

  const canvasRef = useRef<HTMLCanvasElement>(null);
  const animationFrameRef = useRef<number | null>(null);
  const embedCtrlRef = useRef<CleanYouTubeController | null>(null);

  const cleanEmbedUrl = currentSong ? getCleanEmbedUrl(currentSong) : undefined;
  const hasNativeAudioStream = Boolean(
    currentSong &&
      (currentSong.extractedAudioUrl ||
        currentSong.extractedVideoUrl ||
        !cleanEmbedUrl)
  );
  const isEmbedOnly = Boolean(cleanEmbedUrl && !hasNativeAudioStream);

  useEffect(() => {
    setThumbError(false);
    setEmbedError(null);
    setBypassEmbedBlock(false);
    setEmbedPlaying(false);
    setEmbedTime(0);
    setEmbedDuration(currentSong?.duration || 0);
    if (isEmbedOnly && currentSong) {
      let cancelled = false;
      fetchOEmbedMetadata(currentSong.uri).then((meta) => {
        if (cancelled) return;
        if (meta.status === 'not_found') {
          setEmbedError('Could not fetch this song (404 / removed or wrong link).');
        }
      });
      return () => {
        cancelled = true;
      };
    }
  }, [currentSong?.id, currentSong?.uri, isEmbedOnly]);

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;

    const handleTimeUpdate = () => setCurrentTime(audio.currentTime);
    const handleLoadedMetadata = () => setDuration(audio.duration || currentSong?.duration || 0);
    const handleEnded = () => {
      if (repeatMode === 'one') {
        audio.currentTime = 0;
        audio.play().catch(() => {});
      } else {
        onNext();
      }
    };

    audio.addEventListener('timeupdate', handleTimeUpdate);
    audio.addEventListener('loadedmetadata', handleLoadedMetadata);
    audio.addEventListener('ended', handleEnded);

    return () => {
      audio.removeEventListener('timeupdate', handleTimeUpdate);
      audio.removeEventListener('loadedmetadata', handleLoadedMetadata);
      audio.removeEventListener('ended', handleEnded);
    };
  }, [audioRef, currentSong, repeatMode, onNext]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || !isExpanded) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const playingNow = isEmbedOnly ? embedPlaying : isPlaying;
    let angle = 0;
    const render = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      const width = canvas.width;
      const height = canvas.height;
      const numBars = 48;
      const barWidth = (width / numBars) * 0.7;

      for (let i = 0; i < numBars; i++) {
        const factor = playingNow && !audioError
          ? Math.sin(angle + i * 0.25) * 0.4 + Math.cos(angle * 1.5 + i * 0.1) * 0.4 + 0.5
          : 0.08;
        const barHeight = Math.max(4, factor * height * 0.85);
        const x = i * (width / numBars) + (width / numBars - barWidth) / 2;
        const y = height - barHeight;

        const gradient = ctx.createLinearGradient(0, height, 0, 0);
        gradient.addColorStop(0, '#4f46e5');
        gradient.addColorStop(0.5, '#818cf8');
        gradient.addColorStop(1, '#c084fc');

        ctx.fillStyle = gradient;
        ctx.beginPath();
        ctx.roundRect(x, y, barWidth, barHeight, 3);
        ctx.fill();
      }

      if (playingNow && !audioError) angle += 0.06;
      animationFrameRef.current = requestAnimationFrame(render);
    };

    render();
    return () => {
      if (animationFrameRef.current) cancelAnimationFrame(animationFrameRef.current);
    };
  }, [isExpanded, isPlaying, embedPlaying, audioError, isEmbedOnly]);

  if (!currentSong) return null;

  const activeError = isEmbedOnly
    ? (bypassEmbedBlock ? null : embedError)
    : audioError;

  const effectivePlaying = isEmbedOnly ? embedPlaying : isPlaying;
  const effectiveTime = isEmbedOnly ? embedTime : currentTime;
  const effectiveDuration = isEmbedOnly ? (embedDuration || currentSong.duration || 0) : duration;

  const progressPct =
    effectiveDuration > 0
      ? Math.min(100, Math.max(0, (effectiveTime / effectiveDuration) * 100))
      : 0;

  const handleTogglePlayUnified = () => {
    if (isEmbedOnly) {
      if (embedPlaying) {
        embedCtrlRef.current?.pause();
        setEmbedPlaying(false);
      } else {
        embedCtrlRef.current?.play();
        setEmbedPlaying(true);
      }
      return;
    }
    onTogglePlay();
  };

  const handleSeek = (e: React.ChangeEvent<HTMLInputElement>) => {
    const time = parseFloat(e.target.value);
    if (isEmbedOnly) {
      setEmbedTime(time);
      embedCtrlRef.current?.seekTo(time);
      return;
    }
    setCurrentTime(time);
    if (audioRef.current) audioRef.current.currentTime = time;
  };

  const handleVolume = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = parseFloat(e.target.value);
    setVolume(val);
    setIsMuted(val === 0);
    if (isEmbedOnly) {
      embedCtrlRef.current?.setVolume(val);
      embedCtrlRef.current?.setMuted(val === 0);
      return;
    }
    if (audioRef.current) {
      audioRef.current.volume = val;
      audioRef.current.muted = val === 0;
    }
  };

  const toggleMute = () => {
    const next = !isMuted;
    setIsMuted(next);
    if (isEmbedOnly) {
      embedCtrlRef.current?.setMuted(next);
      return;
    }
    if (!audioRef.current) return;
    audioRef.current.muted = next;
  };

  const formatTime = (sec: number) => {
    if (isNaN(sec)) return '0:00';
    const m = Math.floor(sec / 60);
    const s = Math.floor(sec % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const renderArtwork = (sizeClass: string) => {
    if (currentSong.thumbnail && !thumbError) {
      return (
        <img
          src={currentSong.thumbnail}
          alt={currentSong.title}
          referrerPolicy="no-referrer"
          onError={() => setThumbError(true)}
          className={`${sizeClass} object-cover`}
        />
      );
    }
    return (
      <div className={`${sizeClass} bg-gradient-to-br from-indigo-900 via-slate-900 to-purple-950 flex items-center justify-center text-indigo-300`}>
        <Music className="w-5 h-5" />
      </div>
    );
  };

  return (
    <>
      {isEmbedOnly && cleanEmbedUrl && !activeError && (
        <CleanYouTubePlayer
          key={`${currentSong.id}-${embedRetrySeed}`}
          embedUrl={cleanEmbedUrl}
          title={currentSong.title}
          thumbnail={currentSong.thumbnail}
          mode="audio"
          loop={repeatMode === 'one'}
          volume={volume}
          isMuted={isMuted}
          controllerRef={embedCtrlRef}
          onPlayStateChange={(playing) => setEmbedPlaying(playing)}
          onProgress={(ct, dur) => {
            setEmbedTime(ct);
            setEmbedDuration(dur);
          }}
          onEnded={() => {
            if (repeatMode === 'one') {
              embedCtrlRef.current?.seekTo(0);
              embedCtrlRef.current?.play();
            } else {
              onNext();
            }
          }}
          onError={() => setEmbedError('Failed to load audio stream for this link.')}
        />
      )}

      {/* 1. Persistent Mini Player Bar */}
      <div className="fixed bottom-[4.25rem] md:bottom-0 inset-x-2 md:inset-x-0 z-40 rounded-2xl md:rounded-none bg-slate-900/95 border border-slate-800 md:border-x-0 md:border-b-0 backdrop-blur-xl shadow-2xl overflow-hidden">
        {/* Top subtle progress hairline visible on all screen sizes */}
        <div className="h-[2px] w-full bg-slate-800/90 overflow-hidden">
          <div
            className="h-full bg-indigo-500 transition-[width] duration-200"
            style={{ width: `${progressPct}%` }}
          />
        </div>

        <div className="max-w-7xl mx-auto px-3 sm:px-6 py-2 sm:py-2.5 flex items-center justify-between gap-2 sm:gap-4">
          <div
            onClick={() => setIsExpanded(true)}
            className="flex items-center gap-2.5 sm:gap-3 cursor-pointer group min-w-0 flex-1 sm:flex-initial sm:max-w-xs"
          >
            <div className="relative w-10 h-10 sm:w-11 sm:h-11 rounded-lg overflow-hidden shrink-0 border border-slate-800 shadow">
              {renderArtwork('w-full h-full')}
              {effectivePlaying && !activeError && (
                <div className="absolute inset-0 bg-black/25 flex items-center justify-center">
                  <Disc3 className="w-5 h-5 text-indigo-400 animate-spin [animation-duration:3s]" />
                </div>
              )}
            </div>

            <div className="min-w-0">
              <h4 className="text-xs sm:text-sm font-semibold text-white group-hover:text-indigo-400 transition-colors truncate">
                {currentSong.title}
              </h4>
              {activeError ? (
                <p className="text-[11px] text-red-400 truncate flex items-center gap-1">
                  <AlertTriangle className="w-3 h-3 shrink-0" />
                  <span>Failed to fetch audio</span>
                </p>
              ) : (
                <p className="text-[11px] sm:text-xs text-slate-400 truncate">
                  {currentSong.artist || currentSong.sourceLabel || 'Saved Audio'}
                </p>
              )}
            </div>
          </div>

          {/* Center Controls */}
          <div className="flex flex-col items-center gap-1 sm:flex-1 sm:max-w-md shrink-0">
            <div className="flex items-center gap-1.5 sm:gap-3">
              <button
                type="button"
                onClick={() => setIsShuffle(!isShuffle)}
                className={`p-1.5 rounded-full text-xs transition-colors hidden sm:flex ${
                  isShuffle ? 'text-indigo-400' : 'text-slate-400 hover:text-white'
                }`}
                title="Shuffle"
              >
                <Shuffle className="w-4 h-4" />
              </button>

              <button
                type="button"
                onClick={onPrev}
                className="min-h-[38px] min-w-[38px] flex items-center justify-center text-slate-300 hover:text-white transition-colors"
                title="Previous"
              >
                <SkipBack className="w-4 h-4 sm:w-5 sm:h-5" />
              </button>

              {activeError ? (
                <button
                  type="button"
                  onClick={() => {
                    if (isEmbedOnly) {
                      setBypassEmbedBlock(true);
                      setEmbedRetrySeed((prev) => prev + 1);
                    } else {
                      onRetryAudio();
                    }
                  }}
                  className="min-h-[38px] px-3 py-1.5 rounded-full bg-red-600/90 hover:bg-red-500 text-white text-xs font-semibold flex items-center gap-1 shadow-lg"
                  title="Retry Audio"
                >
                  <RefreshCw className="w-3.5 h-3.5" />
                  Retry
                </button>
              ) : (
                <button
                  type="button"
                  onClick={handleTogglePlayUnified}
                  className="min-h-[40px] min-w-[40px] flex items-center justify-center rounded-full bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-600/30 transition-transform active:scale-95"
                  aria-label={effectivePlaying ? 'Pause' : 'Play'}
                >
                  {effectivePlaying ? <Pause className="w-4 h-4 fill-current" /> : <Play className="w-4 h-4 fill-current translate-x-0.5" />}
                </button>
              )}

              <button
                type="button"
                onClick={onNext}
                className="min-h-[38px] min-w-[38px] flex items-center justify-center text-slate-300 hover:text-white transition-colors"
                title="Next"
              >
                <SkipForward className="w-4 h-4 sm:w-5 sm:h-5" />
              </button>

              <button
                type="button"
                onClick={() => {
                  const modes: ('off' | 'all' | 'one')[] = ['off', 'all', 'one'];
                  setRepeatMode(modes[(modes.indexOf(repeatMode) + 1) % modes.length]);
                }}
                className={`p-1.5 rounded-full text-xs transition-colors hidden sm:flex ${
                  repeatMode !== 'off' ? 'text-indigo-400' : 'text-slate-400 hover:text-white'
                }`}
                title={`Repeat: ${repeatMode}`}
              >
                <Repeat className="w-4 h-4" />
              </button>
            </div>

            <div className="w-full hidden sm:flex items-center gap-2 text-[10px] text-slate-500 font-mono tabular-nums">
              <span>{formatTime(effectiveTime)}</span>
              <input
                type="range"
                min="0"
                max={effectiveDuration || 100}
                step="0.1"
                value={effectiveTime}
                onChange={handleSeek}
                className="v2s-slider flex-1 h-1 bg-slate-800 rounded appearance-none cursor-pointer accent-indigo-500"
              />
              <span>{formatTime(effectiveDuration)}</span>
            </div>
          </div>

          {/* Right Action Buttons */}
          <div className="flex items-center gap-1 shrink-0">
            {currentSong.sourceType === 'link' && onEditSong && (
              <button
                type="button"
                onClick={() => onEditSong(currentSong)}
                className="min-h-[36px] min-w-[36px] text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors hidden md:flex items-center justify-center"
                title="Fix / Edit Song Link"
              >
                <Pencil className="w-4 h-4" />
              </button>
            )}

            {!isEmbedOnly && (
              <button
                type="button"
                onClick={onOpenEqualizer}
                className="min-h-[36px] min-w-[36px] text-slate-400 hover:text-indigo-400 rounded-lg hover:bg-slate-800 transition-colors hidden sm:flex items-center justify-center"
                title="Open Equalizer"
              >
                <Sliders className="w-4 h-4" />
              </button>
            )}

            <button
              type="button"
              onClick={() => onToggleFavorite(currentSong.id)}
              className={`min-h-[38px] min-w-[38px] hidden xs:flex sm:flex items-center justify-center rounded-lg transition-colors ${
                currentSong.isFavorite
                  ? 'text-red-500 hover:text-red-400'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`}
              title="Toggle Favorite"
            >
              <Heart className={`w-4 h-4 ${currentSong.isFavorite ? 'fill-current' : ''}`} />
            </button>

            <button
              type="button"
              onClick={() => setIsExpanded(true)}
              className="min-h-[38px] min-w-[38px] flex items-center justify-center text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
              title="Expand Full Player"
            >
              <ChevronUp className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      {/* 2. Full Audio View Modal */}
      {isExpanded && (
        <div className="fixed inset-0 z-50 bg-slate-950/95 backdrop-blur-2xl flex flex-col p-4 sm:p-8 overflow-y-auto v2s-animate-fade">
          <div className="flex items-center justify-between pb-4 border-b border-slate-800/80 gap-2">
            <button
              type="button"
              onClick={() => setIsExpanded(false)}
              className="min-h-[40px] px-2.5 text-slate-400 hover:text-white rounded-xl hover:bg-slate-800 transition-colors flex items-center gap-1.5 text-xs font-semibold shrink-0"
            >
              <ChevronDown className="w-5 h-5" />
              <span className="hidden sm:inline">Minimize</span>
            </button>

            <div className="text-center min-w-0">
              <span className="text-xs uppercase font-bold tracking-wider text-indigo-400 block">
                V2S Audio Deck
              </span>
              <p className="text-[11px] text-slate-500 truncate">
                {currentSong.sourceLabel || currentSong.album || currentSong.folder}
              </p>
            </div>

            <div className="flex items-center gap-1.5 shrink-0">
              {currentSong.sourceType === 'link' && onEditSong && (
                <button
                  type="button"
                  onClick={() => onEditSong(currentSong)}
                  className="min-h-[38px] px-2.5 py-1.5 rounded-lg bg-slate-900 text-slate-200 hover:text-white border border-slate-800 text-xs font-semibold flex items-center gap-1.5"
                >
                  <Pencil className="w-3.5 h-3.5" />
                  <span className="hidden sm:inline">Edit Link</span>
                </button>
              )}
              <button
                type="button"
                onClick={() => setShowQueue((prev) => !prev)}
                className={`min-h-[38px] px-2.5 py-1.5 rounded-lg border text-xs font-semibold flex items-center gap-1.5 transition-colors ${
                  showQueue
                    ? 'bg-indigo-600 text-white border-indigo-500'
                    : 'bg-slate-900 text-slate-200 hover:text-white border-slate-800'
                }`}
              >
                <ListMusic className="w-4 h-4" />
                <span className="hidden sm:inline">Queue ({playlist.length})</span>
              </button>
              {!isEmbedOnly && (
                <button
                  type="button"
                  onClick={onOpenEqualizer}
                  className="min-h-[38px] px-2.5 py-1.5 rounded-lg bg-indigo-500/10 text-indigo-400 hover:bg-indigo-500/20 border border-indigo-500/20 text-xs font-semibold flex items-center gap-1.5"
                >
                  <Sliders className="w-4 h-4" /> EQ
                </button>
              )}
              <button
                type="button"
                onClick={() => setIsExpanded(false)}
                className="min-h-[40px] min-w-[40px] flex items-center justify-center text-slate-400 hover:text-white rounded-xl hover:bg-slate-800"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
          </div>

          <div className="flex-1 flex flex-col lg:flex-row items-center justify-center gap-6 sm:gap-8 py-5 sm:py-6 max-w-5xl mx-auto w-full">
            {isEmbedOnly && activeError ? (
              <div className="w-full max-w-xl aspect-video rounded-2xl border border-red-500/30 bg-slate-900 p-6 flex flex-col items-center justify-center text-center space-y-4 shadow-2xl">
                <div className="w-12 h-12 rounded-2xl bg-red-500/15 text-red-400 flex items-center justify-center">
                  <AlertTriangle className="w-6 h-6" />
                </div>
                <div className="space-y-1">
                  <h3 className="text-base font-bold text-white">Unable to Fetch Song</h3>
                  <p className="text-xs text-slate-300">{activeError}</p>
                </div>
                <div className="flex flex-wrap justify-center gap-2">
                  <button
                    type="button"
                    onClick={() => {
                      setBypassEmbedBlock(true);
                      setEmbedRetrySeed((prev) => prev + 1);
                    }}
                    className="min-h-[40px] px-3.5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold flex items-center gap-1.5"
                  >
                    <RefreshCw className="w-3.5 h-3.5" /> Try Player Anyway
                  </button>
                  {onEditSong && (
                    <button
                      type="button"
                      onClick={() => onEditSong(currentSong)}
                      className="min-h-[40px] px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-white text-xs font-semibold flex items-center gap-1.5"
                    >
                      <Pencil className="w-3.5 h-3.5" /> Edit / Fix Link
                    </button>
                  )}
                  {onDeleteSong && (
                    <button
                      type="button"
                      onClick={() => onDeleteSong(currentSong.id)}
                      className="min-h-[40px] px-3.5 py-2 rounded-xl bg-red-500/15 hover:bg-red-500/25 text-red-300 text-xs font-semibold flex items-center gap-1.5"
                    >
                      <Trash2 className="w-3.5 h-3.5" /> Remove
                    </button>
                  )}
                </div>
              </div>
            ) : (
              <div className="flex flex-col items-center gap-5 w-full max-w-md">
                <div className="relative w-52 h-52 sm:w-72 sm:h-72 rounded-full p-2 bg-gradient-to-tr from-slate-900 via-indigo-950 to-slate-900 border-4 border-slate-800 shadow-2xl flex items-center justify-center">
                  <div
                    className={`w-full h-full rounded-full border-8 border-slate-950 flex items-center justify-center shadow-inner overflow-hidden ${
                      effectivePlaying && !activeError ? 'animate-spin [animation-duration:12s]' : ''
                    }`}
                  >
                    {renderArtwork('w-28 h-28 sm:w-32 sm:h-32 rounded-full border-4 border-slate-900 shadow-md')}
                    <div className="absolute w-6 h-6 rounded-full bg-slate-950 border-2 border-indigo-400 shadow-inner" />
                  </div>
                </div>

                <div className="w-full bg-slate-900/60 p-3 rounded-2xl border border-slate-800/80 shadow-lg">
                  <canvas ref={canvasRef} width={380} height={80} className="w-full h-16 sm:h-20 rounded-xl" />
                </div>
              </div>
            )}

            <div className="flex-1 w-full max-w-md flex flex-col justify-between space-y-5">
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <h2 className="text-xl sm:text-2xl font-bold text-white mb-1 truncate">
                    {currentSong.title}
                  </h2>
                  <p className="text-sm text-indigo-400 font-medium truncate">
                    {currentSong.artist || currentSong.sourceLabel || 'Saved Audio'}
                  </p>
                </div>

                <button
                  type="button"
                  onClick={() => onToggleFavorite(currentSong.id)}
                  className={`p-3 rounded-2xl border transition-colors shrink-0 ${
                    currentSong.isFavorite
                      ? 'bg-red-500/20 text-red-500 border-red-500/30'
                      : 'bg-slate-900 text-slate-400 hover:text-white border-slate-800'
                  }`}
                >
                  <Heart className={`w-5 h-5 ${currentSong.isFavorite ? 'fill-current' : ''}`} />
                </button>
              </div>

              {activeError && !isEmbedOnly && (
                <div className="rounded-2xl bg-red-500/10 border border-red-500/30 p-4 space-y-3 text-xs text-red-200">
                  <div className="flex items-start gap-2">
                    <AlertTriangle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
                    <span>{activeError}</span>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      onClick={onRetryAudio}
                      className="min-h-[38px] px-3 py-1.5 rounded-lg bg-red-500/20 hover:bg-red-500/30 text-white font-semibold flex items-center gap-1.5"
                    >
                      <RefreshCw className="w-3.5 h-3.5" /> Retry
                    </button>
                    {currentSong.sourceType === 'link' && onEditSong && (
                      <button
                        type="button"
                        onClick={() => onEditSong(currentSong)}
                        className="min-h-[38px] px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-white font-semibold flex items-center gap-1.5"
                      >
                        <Pencil className="w-3.5 h-3.5" /> Fix Link
                      </button>
                    )}
                    {onDeleteSong && (
                      <button
                        type="button"
                        onClick={() => onDeleteSong(currentSong.id)}
                        className="min-h-[38px] px-3 py-1.5 rounded-lg bg-slate-900 hover:bg-slate-800 text-red-300 font-semibold flex items-center gap-1.5"
                      >
                        <Trash2 className="w-3.5 h-3.5" /> Remove
                      </button>
                    )}
                  </div>
                </div>
              )}

              {showQueue && (
                <div className="h-44 overflow-y-auto bg-slate-900/60 p-3 rounded-2xl border border-slate-800/80 space-y-1.5">
                  {playlist.length === 0 ? (
                    <p className="text-xs text-slate-500 py-8 text-center">No other songs in queue</p>
                  ) : (
                    playlist.map((track) => (
                      <div
                        key={track.id}
                        onClick={() => onSelectSong(track)}
                        className={`p-2.5 rounded-xl cursor-pointer flex items-center justify-between text-xs transition-colors ${
                          track.id === currentSong.id
                            ? 'bg-indigo-600/20 text-indigo-400 border border-indigo-500/30'
                            : 'hover:bg-slate-800/60 text-slate-300'
                        }`}
                      >
                        <span className="truncate font-medium">{track.title}</span>
                        <span className="font-mono text-[10px] text-slate-500 shrink-0 ml-2">{track.size}</span>
                      </div>
                    ))
                  )}
                </div>
              )}

                  <div className="space-y-1">
                    <input
                      type="range"
                      min="0"
                      max={effectiveDuration || 100}
                      step="0.1"
                      value={effectiveTime}
                      onChange={handleSeek}
                      className="v2s-slider w-full h-2 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-indigo-500"
                    />
                    <div className="flex justify-between text-xs text-slate-400 font-mono tabular-nums">
                      <span>{formatTime(effectiveTime)}</span>
                      <span>{formatTime(effectiveDuration)}</span>
                    </div>
                  </div>

                  <div className="flex items-center justify-between">
                    <button
                      type="button"
                      onClick={() => setIsShuffle(!isShuffle)}
                      className={`min-h-[42px] min-w-[42px] flex items-center justify-center rounded-xl transition-colors ${
                        isShuffle ? 'text-indigo-400 bg-indigo-500/10' : 'text-slate-400 hover:text-white'
                      }`}
                    >
                      <Shuffle className="w-5 h-5" />
                    </button>

                    <div className="flex items-center gap-4">
                      <button
                        type="button"
                        onClick={onPrev}
                        className="p-3 text-slate-300 hover:text-white hover:bg-slate-800 rounded-full transition-colors"
                      >
                        <SkipBack className="w-6 h-6" />
                      </button>

                      <button
                        type="button"
                        onClick={handleTogglePlayUnified}
                        className="p-4 rounded-full bg-indigo-600 hover:bg-indigo-500 text-white shadow-xl shadow-indigo-600/40 transition-transform active:scale-95"
                      >
                        {effectivePlaying ? (
                          <Pause className="w-7 h-7 fill-current" />
                        ) : (
                          <Play className="w-7 h-7 fill-current translate-x-0.5" />
                        )}
                      </button>

                      <button
                        type="button"
                        onClick={onNext}
                        className="p-3 text-slate-300 hover:text-white hover:bg-slate-800 rounded-full transition-colors"
                      >
                        <SkipForward className="w-6 h-6" />
                      </button>
                    </div>

                    <button
                      type="button"
                      onClick={() => {
                        const modes: ('off' | 'all' | 'one')[] = ['off', 'all', 'one'];
                        setRepeatMode(modes[(modes.indexOf(repeatMode) + 1) % modes.length]);
                      }}
                      className={`min-h-[42px] min-w-[42px] flex items-center justify-center rounded-xl transition-colors ${
                        repeatMode !== 'off' ? 'text-indigo-400 bg-indigo-500/10' : 'text-slate-400 hover:text-white'
                      }`}
                    >
                      <Repeat className="w-5 h-5" />
                    </button>
                  </div>

                  <div className="flex items-center gap-3 bg-slate-900/60 px-4 py-2.5 rounded-xl border border-slate-800">
                    <button type="button" onClick={toggleMute} className="text-slate-400 hover:text-white">
                      {isMuted || volume === 0 ? <VolumeX className="w-4 h-4 text-red-400" /> : <Volume2 className="w-4 h-4" />}
                    </button>
                    <input
                      type="range"
                      min="0"
                      max="1"
                      step="0.05"
                      value={isMuted ? 0 : volume}
                      onChange={handleVolume}
                      className="v2s-slider flex-1 h-1.5 bg-slate-800 rounded appearance-none cursor-pointer accent-indigo-500"
                    />
                    <span className="text-xs font-mono text-slate-400 w-9 text-right tabular-nums">
                      {Math.round((isMuted ? 0 : volume) * 100)}%
                    </span>
                  </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
