import React, { useEffect, useId, useImperativeHandle, useRef, useState } from 'react';
import { Loader2, Play, RotateCcw } from 'lucide-react';

export interface CleanYouTubeController {
  play: () => void;
  pause: () => void;
  seekTo: (seconds: number) => void;
  setVolume: (volume01: number) => void;
  setMuted: (muted: boolean) => void;
  setPlaybackRate: (rate: number) => void;
}

interface CleanYouTubePlayerProps {
  embedUrl: string;
  title: string;
  thumbnail?: string;
  mode: 'video' | 'short' | 'audio';
  loop?: boolean;
  volume?: number;
  isMuted?: boolean;
  playbackRate?: number;
  controllerRef?: React.MutableRefObject<CleanYouTubeController | null>;
  onSurfaceClick?: () => void;
  onSurfaceDoubleClick?: () => void;
  onPlayStateChange?: (playing: boolean) => void;
  onProgress?: (currentTime: number, duration: number) => void;
  onEnded?: () => void;
  onReady?: () => void;
  onError?: () => void;
  onAutoplayBlockedChange?: (blocked: boolean) => void;
}

function isYouTubeEmbedUrl(url: string): boolean {
  const lower = url.toLowerCase();
  return lower.includes('youtube.com/embed') || lower.includes('youtube-nocookie.com/embed');
}

export const CleanYouTubePlayer: React.FC<CleanYouTubePlayerProps> = ({
  embedUrl,
  title,
  thumbnail,
  mode,
  loop = false,
  volume = 1,
  isMuted = false,
  playbackRate = 1,
  controllerRef,
  onSurfaceClick,
  onSurfaceDoubleClick,
  onPlayStateChange,
  onProgress,
  onEnded,
  onReady,
  onError,
  onAutoplayBlockedChange,
}) => {
  const widgetId = useId();
  const iframeRef = useRef<HTMLIFrameElement | null>(null);
  const topVeilTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const [isLoaded, setIsLoaded] = useState(false);
  const [hasStartedOnce, setHasStartedOnce] = useState(false);
  const [playerState, setPlayerState] = useState<number>(-1); // -1 unstarted, 0 ended, 1 playing, 2 paused, 3 buffering, 5 cued
  const [showTopVeil, setShowTopVeil] = useState(true);
  const [autoplayBlocked, setAutoplayBlocked] = useState(false);
  const [currentTimeSec, setCurrentTimeSec] = useState(0);

  const isYt = isYouTubeEmbedUrl(embedUrl);

  const sendCommand = (func: string, args: unknown[] = []) => {
    const win = iframeRef.current?.contentWindow;
    if (!win) return;
    try {
      win.postMessage(
        JSON.stringify({
          event: 'command',
          func,
          args,
        }),
        '*'
      );
    } catch {
      // ignore cross-frame messaging errors
    }
  };

  const triggerPlay = () => {
    setAutoplayBlocked(false);
    onAutoplayBlockedChange?.(false);
    sendCommand('playVideo');
  };

  const triggerPause = () => {
    sendCommand('pauseVideo');
  };

  useImperativeHandle(
    controllerRef,
    () => ({
      play: triggerPlay,
      pause: triggerPause,
      seekTo: (seconds: number) => {
        sendCommand('seekTo', [Math.max(0, seconds), true]);
      },
      setVolume: (vol01: number) => {
        sendCommand('setVolume', [Math.round(Math.min(1, Math.max(0, vol01)) * 100)]);
      },
      setMuted: (muted: boolean) => {
        sendCommand(muted ? 'mute' : 'unMute');
      },
      setPlaybackRate: (rate: number) => {
        sendCommand('setPlaybackRate', [rate]);
      },
    }),
    []
  );

  // Reset state when URL changes
  useEffect(() => {
    setIsLoaded(false);
    setHasStartedOnce(false);
    setPlayerState(-1);
    setShowTopVeil(true);
    setAutoplayBlocked(false);
    setCurrentTimeSec(0);
    onAutoplayBlockedChange?.(false);
    return () => {
      if (topVeilTimerRef.current) clearTimeout(topVeilTimerRef.current);
    };
  }, [embedUrl]);

  // Keep YouTube audio/volume/speed synced when props change
  useEffect(() => {
    if (!isYt || !isLoaded) return;
    sendCommand('setVolume', [Math.round(Math.min(1, Math.max(0, volume)) * 100)]);
  }, [volume, isYt, isLoaded]);

  useEffect(() => {
    if (!isYt || !isLoaded) return;
    sendCommand(isMuted ? 'mute' : 'unMute');
  }, [isMuted, isYt, isLoaded]);

  useEffect(() => {
    if (!isYt || !isLoaded) return;
    sendCommand('setPlaybackRate', [playbackRate]);
  }, [playbackRate, isYt, isLoaded]);

  // Autoplay block detector (helps mobile Safari / strict browsers surface a clean Play button)
  useEffect(() => {
    if (!isYt || !isLoaded || hasStartedOnce) return;
    const timer = setTimeout(() => {
      if (!hasStartedOnce && playerState !== 1 && playerState !== 3) {
        setAutoplayBlocked(true);
        onAutoplayBlockedChange?.(true);
      }
    }, 1400);
    return () => clearTimeout(timer);
  }, [isYt, isLoaded, hasStartedOnce, playerState]);

  const callbacksRef = useRef({
    loop,
    volume,
    isMuted,
    onProgress,
    onPlayStateChange,
    onEnded,
    onReady,
    onError,
    onAutoplayBlockedChange,
  });
  callbacksRef.current = {
    loop,
    volume,
    isMuted,
    onProgress,
    onPlayStateChange,
    onEnded,
    onReady,
    onError,
    onAutoplayBlockedChange,
  };

  // Listen to YouTube IFrame API postMessage events
  useEffect(() => {
    if (!isYt) return;

    const handleMessage = (event: MessageEvent) => {
      if (
        iframeRef.current &&
        event.source !== iframeRef.current.contentWindow
      ) {
        return;
      }
      if (typeof event.data !== 'string') return;
      let parsed: {
        event?: string;
        info?: {
          playerState?: number;
          currentTime?: number;
          duration?: number;
        };
      };
      try {
        parsed = JSON.parse(event.data);
      } catch {
        return;
      }

      const cb = callbacksRef.current;

      if (parsed.event === 'onReady') {
        setIsLoaded(true);
        cb.onReady?.();
        sendCommand('setVolume', [Math.round(Math.min(1, Math.max(0, cb.volume)) * 100)]);
        sendCommand(cb.isMuted ? 'mute' : 'unMute');
        sendCommand('playVideo');
        return;
      }

      if (parsed.event === 'onError') {
        cb.onError?.();
        return;
      }

      if (parsed.event === 'infoDelivery' && parsed.info) {
        const { playerState: nextState, currentTime, duration } = parsed.info;

        if (typeof currentTime === 'number' && Number.isFinite(currentTime)) {
          setCurrentTimeSec(currentTime);
        }
        if (
          typeof currentTime === 'number' &&
          typeof duration === 'number' &&
          duration > 0
        ) {
          cb.onProgress?.(currentTime, duration);
        }

        if (typeof nextState === 'number') {
          setPlayerState(nextState);
          if (nextState === 1) {
            setHasStartedOnce(true);
            setAutoplayBlocked(false);
            cb.onAutoplayBlockedChange?.(false);
            cb.onPlayStateChange?.(true);

            // Mask top YouTube title/channel bar while it fades out on start/resume
            setShowTopVeil(true);
            if (topVeilTimerRef.current) clearTimeout(topVeilTimerRef.current);
            topVeilTimerRef.current = setTimeout(() => {
              setShowTopVeil(false);
            }, 3900);
          } else if (nextState === 2) {
            cb.onPlayStateChange?.(false);
          } else if (nextState === 0) {
            if (cb.loop) {
              sendCommand('seekTo', [0, true]);
              sendCommand('playVideo');
            } else {
              cb.onPlayStateChange?.(false);
              cb.onEnded?.();
            }
          }
        }
      }
    };

    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, [isYt, embedUrl]);

  // Periodically register widget listener after iframe loads until info arrives
  useEffect(() => {
    if (!isYt || !isLoaded) return;
    const ping = () => {
      try {
        iframeRef.current?.contentWindow?.postMessage(
          JSON.stringify({
            event: 'listening',
            id: widgetId,
            channel: 'widget',
          }),
          '*'
        );
      } catch {
        // ignore
      }
    };
    ping();
    const interval = setInterval(ping, 500);
    const stopTimer = setTimeout(() => clearInterval(interval), 4000);
    return () => {
      clearInterval(interval);
      clearTimeout(stopTimer);
    };
  }, [isYt, isLoaded, widgetId, embedUrl]);

  // Audio-only hidden host mode (used inside AudioPlayerDeck so zero YouTube video chrome ever shows)
  if (mode === 'audio') {
    return (
      <div
        aria-hidden="true"
        className="w-56 h-32 fixed bottom-24 right-4 opacity-[0.001] pointer-events-none overflow-hidden -z-10"
      >
        <iframe
          ref={iframeRef}
          src={embedUrl}
          title={title}
          allow="autoplay; encrypted-media; picture-in-picture"
          referrerPolicy="strict-origin-when-cross-origin"
          onLoad={() => {
            setIsLoaded(true);
            onReady?.();
          }}
          onError={() => onError?.()}
          className="w-full h-full border-0"
        />
      </div>
    );
  }

  // Non-YouTube embeds (Vimeo, SoundCloud, etc.)
  if (!isYt) {
    return (
      <div className="relative w-full h-full bg-black overflow-hidden">
        <iframe
          ref={iframeRef}
          src={embedUrl}
          title={title}
          allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
          allowFullScreen
          onLoad={() => {
            setIsLoaded(true);
            onReady?.();
          }}
          onError={() => onError?.()}
          className="w-full h-full border-0 bg-black"
        />
      </div>
    );
  }

  const isPlaying = playerState === 1;
  const isPaused = hasStartedOnce && playerState === 2;
  const isEnded = hasStartedOnce && playerState === 0 && !loop;
  const isInitialLoading = !hasStartedOnce && playerState !== 1 && !autoplayBlocked;

  return (
    <div className="relative w-full h-full bg-black overflow-hidden select-none">
      {/* Embedded YouTube video frame with pointer-events disabled once started so hover chrome never wakes */}
      <iframe
        ref={iframeRef}
        src={embedUrl}
        title={title}
        allow="autoplay; encrypted-media; picture-in-picture"
        referrerPolicy="strict-origin-when-cross-origin"
        onLoad={() => {
          setIsLoaded(true);
          onReady?.();
        }}
        onError={() => onError?.()}
        className={`w-full h-full border-0 bg-black ${
          hasStartedOnce ? 'pointer-events-none' : autoplayBlocked ? 'pointer-events-auto' : 'pointer-events-none'
        }`}
      />

      {/* Edge masks when not started or during initial 3.8s fade-out of YouTube's top channel bar */}
      {(showTopVeil || !isPlaying || currentTimeSec < 4.2) && (
        <div
          className="absolute top-0 inset-x-0 h-20 bg-gradient-to-b from-black via-black/95 to-transparent pointer-events-none z-10 transition-opacity duration-500"
        />
      )}

      {/* Bottom edge mask covers YouTube watermark / bottom bar if unstarted */}
      {!hasStartedOnce && (
        <div
          className="absolute bottom-0 inset-x-0 h-28 bg-gradient-to-t from-black via-black/95 to-transparent pointer-events-none z-10"
        />
      )}

      {/* Initial loading cover (hides top channel bar & bottom More Videos completely before playback starts) */}
      {isInitialLoading && (
        <div className="absolute inset-0 z-20 bg-black flex flex-col items-center justify-center gap-3">
          {thumbnail && (
            <img
              src={thumbnail}
              alt=""
              referrerPolicy="no-referrer"
              className="absolute inset-0 w-full h-full object-cover opacity-20 blur-sm pointer-events-none"
            />
          )}
          <Loader2 className="relative z-10 w-8 h-8 text-indigo-400 animate-spin" />
          <span className="relative z-10 text-xs text-slate-300 font-medium">
            Starting stream...
          </span>
        </div>
      )}

      {/* Clean V2S Play / Resume / Replay Scrim (covers YouTube Pause "More Videos" & Channel Header completely) */}
      {(isPaused || isEnded || autoplayBlocked) && (
        <div
          onClick={() => {
            if (isEnded) {
              sendCommand('seekTo', [0, true]);
            }
            triggerPlay();
          }}
          className="absolute inset-0 z-20 bg-black/80 backdrop-blur-md flex flex-col items-center justify-center gap-3 cursor-pointer"
        >
          {thumbnail && (
            <img
              src={thumbnail}
              alt=""
              referrerPolicy="no-referrer"
              className="absolute inset-0 w-full h-full object-cover opacity-20 pointer-events-none"
            />
          )}
          <div className="relative z-10 w-16 h-16 rounded-full bg-indigo-600/90 hover:bg-indigo-500 text-white flex items-center justify-center shadow-2xl border border-white/15 transition-transform active:scale-95">
            {isEnded ? (
              <RotateCcw className="w-7 h-7" />
            ) : (
              <Play className="w-8 h-8 fill-current translate-x-0.5" />
            )}
          </div>
          <span className="relative z-10 text-xs font-semibold text-white/90 bg-black/50 px-3 py-1 rounded-full">
            {isEnded ? 'Tap to Replay' : 'Tap to Play'}
          </span>
        </div>
      )}

      {/* Transparent interactive surface during active playback */}
      {isPlaying && (
        <div
          onClick={() => onSurfaceClick?.()}
          onDoubleClick={() => onSurfaceDoubleClick?.()}
          className="absolute inset-0 z-10 cursor-pointer"
        />
      )}
    </div>
  );
};
