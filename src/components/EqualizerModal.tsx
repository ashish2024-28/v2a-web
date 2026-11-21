import React, { useState } from 'react';
import { Sliders, Check, RotateCcw, X, Volume2, Sparkles } from 'lucide-react';
import { EQUALIZER_PRESETS, EQUALIZER_FREQUENCIES } from '../data/sampleMedia';
import { EqualizerPreset } from '../types/media';

interface EqualizerModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentGains: number[];
  onGainsChange: (newGains: number[]) => void;
  bassBoost: number;
  onBassBoostChange: (val: number) => void;
  virtualizer: number;
  onVirtualizerChange: (val: number) => void;
  enabled: boolean;
  onToggleEnabled: () => void;
}

export const EqualizerModal: React.FC<EqualizerModalProps> = ({
  isOpen,
  onClose,
  currentGains,
  onGainsChange,
  bassBoost,
  onBassBoostChange,
  virtualizer,
  onVirtualizerChange,
  enabled,
  onToggleEnabled,
}) => {
  const [selectedPresetId, setSelectedPresetId] = useState<string>('flat');

  if (!isOpen) return null;

  const handleSelectPreset = (preset: EqualizerPreset) => {
    setSelectedPresetId(preset.id);
    onGainsChange([...preset.gains]);
  };

  const handleBandChange = (index: number, val: number) => {
    const updated = [...currentGains];
    updated[index] = val;
    setSelectedPresetId('custom');
    onGainsChange(updated);
  };

  const handleReset = () => {
    const flat = EQUALIZER_PRESETS.find((p) => p.id === 'flat')!;
    handleSelectPreset(flat);
    onBassBoostChange(0);
    onVirtualizerChange(0);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center sm:p-4 bg-black/80 backdrop-blur-md v2s-animate-fade">
      <div className="relative w-full max-w-2xl bg-slate-900 border border-slate-800 rounded-t-2xl sm:rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[92dvh] v2s-animate-modal">
        {/* Header */}
        <div className="flex items-center justify-between gap-3 px-4 sm:px-6 py-4 border-b border-slate-800 bg-slate-900/60">
          <div className="flex items-center gap-3 min-w-0">
            <div className="p-2 bg-indigo-500/20 text-indigo-400 rounded-xl shrink-0">
              <Sliders className="w-5 h-5" />
            </div>
            <div className="min-w-0">
              <h2 className="text-base sm:text-lg font-bold text-white truncate">
                V2S Studio Equalizer
              </h2>
              <p className="text-xs text-slate-400 truncate">
                10-Band DSP &middot; Hardware-calibrated 3D soundstage
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            <button
              type="button"
              onClick={onToggleEnabled}
              className={`min-h-[38px] px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors whitespace-nowrap ${
                enabled
                  ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                  : 'bg-slate-800 text-slate-400 border border-slate-700'
              }`}
            >
              {enabled ? 'DSP Active' : 'Bypassed'}
            </button>
            <button
              type="button"
              onClick={onClose}
              className="min-h-[40px] min-w-[40px] flex items-center justify-center text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
              aria-label="Close Equalizer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Body */}
        <div className="p-4 sm:p-6 space-y-5 overflow-y-auto flex-1">
          {/* Preset Buttons */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="text-xs font-medium text-slate-400 uppercase tracking-wider">
                Sound Profiles
              </label>
              <button
                type="button"
                onClick={handleReset}
                className="text-xs text-slate-400 hover:text-indigo-400 flex items-center gap-1 transition-colors min-h-[32px] px-2"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                Reset Flat
              </button>
            </div>
            <div className="flex flex-wrap gap-2">
              {EQUALIZER_PRESETS.map((preset) => {
                const isActive = selectedPresetId === preset.id;
                return (
                  <button
                    key={preset.id}
                    type="button"
                    onClick={() => handleSelectPreset(preset)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors flex items-center gap-1.5 ${
                      isActive
                        ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30 border border-indigo-500'
                        : 'bg-slate-800/80 text-slate-300 hover:bg-slate-800 hover:text-white border border-slate-700/60'
                    }`}
                  >
                    {isActive && <Check className="w-3.5 h-3.5" />}
                    {preset.name}
                  </button>
                );
              })}
            </div>
          </div>

          {/* 10-Band Sliders (Horizontal scroll safe on narrow phones) */}
          <div className="bg-slate-950/60 p-4 rounded-xl border border-slate-800/80 overflow-x-auto">
            <div className="min-w-[480px]">
              <div className="flex justify-between text-[10px] text-slate-500 mb-2 px-2 font-mono">
                <span>+12 dB</span>
                <span>0 dB</span>
                <span>-12 dB</span>
              </div>

              <div className="grid grid-cols-10 gap-2 h-44 items-end pb-2">
                {currentGains.map((gain, idx) => (
                  <div key={idx} className="flex flex-col items-center h-full justify-between">
                    <span className="text-[10px] font-mono text-slate-400 tabular-nums">
                      {gain > 0 ? `+${gain}` : gain}
                    </span>

                    <div className="relative w-full flex justify-center h-28 items-center">
                      <input
                        type="range"
                        min="-12"
                        max="12"
                        step="1"
                        value={gain}
                        disabled={!enabled}
                        onChange={(e) => handleBandChange(idx, parseInt(e.target.value, 10))}
                        className="v2s-slider w-28 -rotate-90 appearance-none bg-slate-800 h-1.5 rounded-lg cursor-pointer accent-indigo-500 disabled:opacity-40"
                      />
                    </div>

                    <span className="text-[10px] text-slate-400 font-mono text-center">
                      {EQUALIZER_FREQUENCIES[idx]}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Bass Boost and Virtualizer Sliders */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-sm font-semibold text-white">
                  <Volume2 className="w-4 h-4 text-indigo-400" />
                  Sub-Bass Booster
                </div>
                <span className="text-xs font-mono text-indigo-400 font-bold tabular-nums">
                  {bassBoost}%
                </span>
              </div>
              <input
                type="range"
                min="0"
                max="100"
                value={bassBoost}
                disabled={!enabled}
                onChange={(e) => onBassBoostChange(parseInt(e.target.value, 10))}
                className="v2s-slider w-full appearance-none bg-slate-800 h-2 rounded-lg cursor-pointer accent-indigo-500 disabled:opacity-40"
              />
              <p className="text-[11px] text-slate-500">
                Enhances deep 30-100Hz low frequencies without clipping.
              </p>
            </div>

            <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-sm font-semibold text-white">
                  <Sparkles className="w-4 h-4 text-purple-400" />
                  3D Spatial Virtualizer
                </div>
                <span className="text-xs font-mono text-purple-400 font-bold tabular-nums">
                  {virtualizer}%
                </span>
              </div>
              <input
                type="range"
                min="0"
                max="100"
                value={virtualizer}
                disabled={!enabled}
                onChange={(e) => onVirtualizerChange(parseInt(e.target.value, 10))}
                className="v2s-slider w-full appearance-none bg-slate-800 h-2 rounded-lg cursor-pointer accent-purple-500 disabled:opacity-40"
              />
              <p className="text-[11px] text-slate-500">
                Broadens the stereo soundstage for headphone immersion.
              </p>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="px-4 sm:px-6 py-3 bg-slate-900 border-t border-slate-800 flex flex-wrap justify-between items-center gap-2 text-xs text-slate-500">
          <span className="hidden sm:inline">Real-time Web Audio DSP &amp; ExoPlayer AudioProcessor</span>
          <button
            type="button"
            onClick={onClose}
            className="ml-auto min-h-[40px] px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white font-semibold rounded-lg transition-colors"
          >
            Apply &amp; Close
          </button>
        </div>
      </div>
    </div>
  );
};
