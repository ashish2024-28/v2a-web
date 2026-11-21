import React from 'react';
import { Smartphone, X, ArrowDownToLine, CheckCircle2 } from 'lucide-react';

interface ApkHubModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ApkHubModal: React.FC<ApkHubModalProps> = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center sm:p-4 bg-black/80 backdrop-blur-md v2s-animate-fade">
      <div className="relative w-full max-w-lg bg-slate-900 border border-slate-800 rounded-t-2xl sm:rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90dvh] v2s-animate-modal">
        <div className="flex items-center justify-between gap-3 px-5 py-4 border-b border-slate-800">
          <div className="flex items-center gap-3 min-w-0">
            <div className="p-2.5 bg-indigo-500/15 text-indigo-400 rounded-xl shrink-0">
              <Smartphone className="w-5 h-5" />
            </div>
            <div className="min-w-0">
              <h2 className="text-base font-bold text-white truncate">V2S for Android</h2>
              <p className="text-xs text-slate-400 truncate">
                Play videos, shorts, songs &amp; playlists offline or in background
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="min-h-[40px] min-w-[40px] flex items-center justify-center text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors shrink-0"
            aria-label="Close modal"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-5 space-y-5 overflow-y-auto flex-1">
          <div className="p-4 bg-slate-950/80 border border-slate-800 rounded-2xl flex flex-col sm:flex-row items-center justify-between gap-4">
            <div className="space-y-1 text-center sm:text-left">
              <h3 className="text-base font-bold text-white">V2S Android App</h3>
              <p className="text-xs text-slate-400">
                Android 7.0+ &middot; Background player &amp; device media scanner
              </p>
            </div>

            <a
              href="/v2s-debug.apk"
              download="V2S-Media-Player.apk"
              className="w-full sm:w-auto min-h-[44px] px-5 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold rounded-xl flex items-center justify-center gap-2 shadow-lg shadow-indigo-600/25 transition-colors whitespace-nowrap"
            >
              <ArrowDownToLine className="w-4 h-4" />
              Download APK
            </a>
          </div>

          <div className="space-y-2.5 text-xs text-slate-300">
            <div className="flex items-start gap-2.5">
              <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
              <span>Plays local phone videos &amp; songs alongside extracted web links.</span>
            </div>
            <div className="flex items-start gap-2.5">
              <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
              <span>Continues audio &amp; video playback in the background with top notification controls.</span>
            </div>
            <div className="flex items-start gap-2.5">
              <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
              <span>Allow &quot;Install unknown apps&quot; when prompted to install the APK on your phone.</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
