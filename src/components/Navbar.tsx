import React from 'react';
import { Play, Plus, Film, Flame, Music, Home, ListMusic } from 'lucide-react';

interface NavbarProps {
  activeTab: string;
  onTabChange: (tab: string) => void;
  onOpenStreamModal: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  activeTab,
  onTabChange,
  onOpenStreamModal,
}) => {
  const navTabs = [
    { id: 'all', label: 'Home', icon: Home },
    { id: 'videos', label: 'Videos', icon: Film },
    { id: 'shorts', label: 'Shorts', icon: Flame },
    { id: 'songs', label: 'Songs', icon: Music },
    { id: 'playlists', label: 'Playlists', icon: ListMusic },
  ];

  return (
    <>
      {/* Clean 3-Zone Top Bar */}
      <header className="sticky top-0 z-30 bg-slate-950/90 backdrop-blur-xl border-b border-slate-800/80 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto h-14 sm:h-16 flex items-center justify-between gap-8">
          {/* Zone 1: Brand Wordmark */}
          <button
            type="button"
            onClick={() => onTabChange('all')}
            className="flex items-center gap-2.5 cursor-pointer group shrink-0 min-h-[40px]"
          >
            <div className="w-8 h-8 rounded-xl bg-indigo-600 flex items-center justify-center text-white shadow-md shadow-indigo-600/25 group-hover:bg-indigo-500 transition-colors">
              <Play className="w-4 h-4 fill-current translate-x-0.5" />
            </div>
            <span className="text-lg font-extrabold tracking-tight text-white whitespace-nowrap shrink-0">
              V2S
            </span>
          </button>

          {/* Zone 2: Clean Typography Nav Links (Desktop & Tablet) */}
          <nav className="hidden md:flex items-center gap-7" aria-label="Primary navigation">
            {navTabs.map((tab) => {
              const isActive =
                activeTab === tab.id ||
                (tab.id === 'all' &&
                  (activeTab === 'favorites' || activeTab === 'history' || activeTab === 'folders'));
              return (
                <button
                  key={tab.id}
                  type="button"
                  onClick={() => onTabChange(tab.id)}
                  className={`relative py-2 text-sm font-semibold whitespace-nowrap shrink-0 transition-colors ${
                    isActive ? 'text-white' : 'text-slate-400 hover:text-slate-100'
                  }`}
                >
                  {tab.label}
                  {isActive && (
                    <span className="absolute inset-x-0 -bottom-0.5 h-0.5 rounded-full bg-indigo-500" />
                  )}
                </button>
              );
            })}
          </nav>

          {/* Zone 3: Single Primary Action */}
          <div className="flex items-center shrink-0">
            <button
              type="button"
              onClick={onOpenStreamModal}
              className="min-h-[40px] px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs sm:text-sm font-semibold flex items-center gap-1.5 whitespace-nowrap shrink-0 shadow-md shadow-indigo-600/25 transition-colors active:scale-[0.98]"
            >
              <Plus className="w-4 h-4 shrink-0" />
              <span>Add Link</span>
            </button>
          </div>
        </div>
      </header>

      {/* Native Thumb-Zone Bottom Tab Bar (Mobile) */}
      <nav
        className="md:hidden fixed bottom-0 inset-x-0 z-40 h-16 bg-slate-950/95 backdrop-blur-xl border-t border-slate-800/80 grid grid-cols-5 items-center px-1"
        aria-label="Mobile navigation"
      >
        {navTabs.map((tab) => {
          const Icon = tab.icon;
          const isActive =
            activeTab === tab.id ||
            (tab.id === 'all' &&
              (activeTab === 'favorites' || activeTab === 'history' || activeTab === 'folders'));
          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => onTabChange(tab.id)}
              className={`min-h-[44px] flex flex-col items-center justify-center gap-1 rounded-xl transition-colors ${
                isActive ? 'text-indigo-400' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <Icon className="w-5 h-5 shrink-0" />
              <span className="text-[11px] font-semibold leading-none whitespace-nowrap">
                {tab.label}
              </span>
            </button>
          );
        })}
      </nav>
    </>
  );
};
