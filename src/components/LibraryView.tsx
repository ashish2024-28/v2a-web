import React, { useState } from 'react';
import {
  Film, Music, Flame, Folder, ListMusic, Heart, History,
  Search, Grid, List, Play, Plus, HardDrive,
  Trash2, ArrowUpDown, Link2, FolderOpen, Pencil, ListPlus, X,
  ChevronDown, ChevronUp, AlertCircle
} from 'lucide-react';
import { MediaItem, MediaType, Playlist } from '../types/media';
import { validateMediaUrl } from '../utils/mediaParser';

interface LibraryViewProps {
  mediaItems: MediaItem[];
  playlists: Playlist[];
  onPlayVideo: (item: MediaItem) => void;
  onPlaySong: (item: MediaItem) => void;
  onSelectShort: (item: MediaItem) => void;
  onToggleFavorite: (id: string) => void;
  onEditItem: (item: MediaItem) => void;
  onDeleteItem: (id: string) => void;
  onCreatePlaylist: (name: string, description: string) => void;
  onImportPlaylistLink: (
    url: string,
    customTitle?: string,
    preferredType?: MediaType,
    forceOverride?: boolean
  ) => Promise<{ ok: boolean; error?: string; allowForce?: boolean }>;
  onAddToPlaylist?: (itemId: string, playlistId: string) => void;
  onRemoveFromPlaylist?: (itemId: string, playlistId: string) => void;
  onPlayPlaylist?: (playlist: Playlist, startItem?: MediaItem) => void;
  onDeletePlaylist?: (playlistId: string) => void;
  onOpenAddLink: () => void;
  onOpenDeviceImport: () => void;
  activeTab: string;
  onTabChange: (tab: string) => void;
}

export const LibraryView: React.FC<LibraryViewProps> = ({
  mediaItems,
  playlists,
  onPlayVideo,
  onPlaySong,
  onSelectShort,
  onToggleFavorite,
  onEditItem,
  onDeleteItem,
  onCreatePlaylist,
  onImportPlaylistLink,
  onAddToPlaylist,
  onRemoveFromPlaylist,
  onPlayPlaylist,
  onDeletePlaylist,
  onOpenAddLink,
  onOpenDeviceImport,
  activeTab,
  onTabChange,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [sortBy, setSortBy] = useState<'recent' | 'name' | 'duration'>('recent');
  const [showNewPlaylistModal, setShowNewPlaylistModal] = useState(false);
  const [newPlaylistName, setNewPlaylistName] = useState('');
  const [newPlaylistDesc, setNewPlaylistDesc] = useState('');

  const [showImportPlaylistModal, setShowImportPlaylistModal] = useState(false);
  const [playlistLinkInput, setPlaylistLinkInput] = useState('');
  const [playlistTitleInput, setPlaylistTitleInput] = useState('');
  const [playlistTypeInput, setPlaylistTypeInput] = useState<MediaType>('video');
  const [playlistImporting, setPlaylistImporting] = useState(false);
  const [playlistImportError, setPlaylistImportError] = useState<string | null>(null);
  const [playlistAllowForce, setPlaylistAllowForce] = useState(false);

  const [expandedPlaylistId, setExpandedPlaylistId] = useState<string | null>(null);
  const [brokenThumbIds, setBrokenThumbIds] = useState<Record<string, boolean>>({});
  const [playlistPickerItemId, setPlaylistPickerItemId] = useState<string | null>(null);

  const isHomeScope =
    activeTab === 'all' ||
    activeTab === 'favorites' ||
    activeTab === 'history' ||
    activeTab === 'folders';

  const filteredItems = mediaItems.filter((item) => {
    if (activeTab === 'videos' && item.type !== 'video') return false;
    if (activeTab === 'songs' && item.type !== 'song') return false;
    if (activeTab === 'shorts' && item.type !== 'short') return false;
    if (activeTab === 'favorites' && !item.isFavorite) return false;
    if (activeTab === 'history' && !item.lastPlayed) return false;

    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      const matchTitle = item.title.toLowerCase().includes(q);
      const matchArtist = item.artist?.toLowerCase().includes(q);
      const matchFolder = item.folder?.toLowerCase().includes(q);
      const matchSource = item.sourceLabel?.toLowerCase().includes(q);
      if (!matchTitle && !matchArtist && !matchFolder && !matchSource) return false;
    }

    return true;
  });

  const sortedItems = [...filteredItems].sort((a, b) => {
    if (sortBy === 'recent') return (b.createdAt || 0) - (a.createdAt || 0);
    if (sortBy === 'name') return a.title.localeCompare(b.title);
    if (sortBy === 'duration') return b.duration - a.duration;
    return 0;
  });

  const foldersMap = mediaItems.reduce<Record<string, MediaItem[]>>((acc, item) => {
    const f = item.folder || item.sourceLabel || 'Saved Media';
    if (!acc[f]) acc[f] = [];
    acc[f].push(item);
    return acc;
  }, {});

  const formatDuration = (sec: number) => {
    if (!sec) return 'Stream';
    const m = Math.floor(sec / 60);
    const s = Math.floor(sec % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const handleOpenItem = (item: MediaItem) => {
    if (item.type === 'video') onPlayVideo(item);
    else if (item.type === 'short') onSelectShort(item);
    else onPlaySong(item);
  };

  const handleCreatePlaylistSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newPlaylistName.trim()) return;
    onCreatePlaylist(newPlaylistName.trim(), newPlaylistDesc.trim());
    setNewPlaylistName('');
    setNewPlaylistDesc('');
    setShowNewPlaylistModal(false);
  };

  const submitPlaylistImport = async (forceOverride: boolean) => {
    const validation = validateMediaUrl(playlistLinkInput);
    if (!validation.valid) {
      setPlaylistImportError(validation.reason || 'Paste a valid playlist link.');
      return;
    }
    setPlaylistImporting(true);
    setPlaylistImportError(null);
    try {
      const res = await onImportPlaylistLink(
        validation.normalizedUrl,
        playlistTitleInput.trim() || undefined,
        playlistTypeInput,
        forceOverride
      );
      if (res.ok) {
        setPlaylistLinkInput('');
        setPlaylistTitleInput('');
        setPlaylistAllowForce(false);
        setShowImportPlaylistModal(false);
      } else {
        setPlaylistImportError(
          res.error || 'Could not fetch this playlist link. Check that the link is public.'
        );
        setPlaylistAllowForce(Boolean(res.allowForce));
      }
    } finally {
      setPlaylistImporting(false);
    }
  };

  const renderPoster = (item: MediaItem, compact = false) => {
    const showImg = Boolean(item.thumbnail) && !brokenThumbIds[item.id];
    if (showImg) {
      return (
        <img
          src={item.thumbnail}
          alt={item.title}
          referrerPolicy="no-referrer"
          onError={() => setBrokenThumbIds((prev) => ({ ...prev, [item.id]: true }))}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        />
      );
    }

    const bg =
      item.type === 'song'
        ? 'from-emerald-950 via-slate-900 to-teal-950'
        : item.type === 'short'
        ? 'from-rose-950 via-slate-900 to-orange-950'
        : 'from-indigo-950 via-slate-900 to-slate-950';

    return (
      <div className={`w-full h-full bg-gradient-to-br ${bg} flex items-center justify-center`}>
        {item.type === 'song' ? (
          <Music className={`${compact ? 'w-5 h-5' : 'w-10 h-10'} text-emerald-400/70`} />
        ) : item.type === 'short' ? (
          <Flame className={`${compact ? 'w-5 h-5' : 'w-10 h-10'} text-orange-400/70`} />
        ) : (
          <Film className={`${compact ? 'w-5 h-5' : 'w-10 h-10'} text-indigo-400/70`} />
        )}
      </div>
    );
  };

  return (
    <div className="space-y-5">
      {/* Clean Contextual Toolbar (No duplicated main nav tabs) */}
      {activeTab !== 'playlists' && (
        <div className="flex flex-col sm:flex-row gap-3 sm:items-center justify-between">
          {isHomeScope ? (
            <div className="flex items-center gap-1 bg-slate-900/80 p-1 rounded-xl border border-slate-800/80 self-start overflow-x-auto max-w-full">
              {[
                { id: 'all', label: 'All', icon: HardDrive },
                { id: 'favorites', label: 'Favorites', icon: Heart },
                { id: 'history', label: 'History', icon: History },
                { id: 'folders', label: 'Sources', icon: Folder },
              ].map((tab) => {
                const Icon = tab.icon;
                const isActive = activeTab === tab.id;
                return (
                  <button
                    key={tab.id}
                    type="button"
                    onClick={() => onTabChange(tab.id)}
                    className={`min-h-[36px] px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 shrink-0 whitespace-nowrap transition-colors ${
                      isActive
                        ? 'bg-indigo-600 text-white shadow-sm'
                        : 'text-slate-400 hover:text-white'
                    }`}
                  >
                    <Icon className="w-3.5 h-3.5" />
                    {tab.label}
                  </button>
                );
              })}
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <h2 className="text-base sm:text-lg font-bold text-white capitalize">
                {activeTab}
              </h2>
              <span className="text-xs text-slate-400 tabular-nums">
                &middot; {sortedItems.length} items
              </span>
            </div>
          )}

          <div className="flex items-center gap-2">
            <div className="relative flex-1 sm:w-56">
              <Search className="w-4 h-4 text-slate-500 absolute left-3 top-2.5" />
              <input
                type="text"
                placeholder="Search library..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full min-h-[38px] pl-9 pr-3 py-1.5 bg-slate-900/90 border border-slate-800 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors"
              />
            </div>

            <div className="relative flex items-center bg-slate-900/90 border border-slate-800 rounded-xl px-2.5 min-h-[38px]">
              <ArrowUpDown className="w-3.5 h-3.5 text-slate-500 mr-1.5 shrink-0" />
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value as 'recent' | 'name' | 'duration')}
                className="bg-transparent text-xs text-slate-300 focus:outline-none cursor-pointer pr-1"
              >
                <option value="recent" className="bg-slate-900">Newest</option>
                <option value="name" className="bg-slate-900">Name</option>
                <option value="duration" className="bg-slate-900">Length</option>
              </select>
            </div>

            <div className="flex bg-slate-900/90 border border-slate-800 rounded-xl p-0.5">
              <button
                type="button"
                onClick={() => setViewMode('grid')}
                className={`min-h-[34px] min-w-[34px] flex items-center justify-center rounded-lg transition-colors ${
                  viewMode === 'grid' ? 'bg-indigo-600 text-white' : 'text-slate-500 hover:text-white'
                }`}
                title="Grid View"
              >
                <Grid className="w-3.5 h-3.5" />
              </button>
              <button
                type="button"
                onClick={() => setViewMode('list')}
                className={`min-h-[34px] min-w-[34px] flex items-center justify-center rounded-lg transition-colors ${
                  viewMode === 'list' ? 'bg-indigo-600 text-white' : 'text-slate-500 hover:text-white'
                }`}
                title="List View"
              >
                <List className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Sources / Folders View */}
      {activeTab === 'folders' && (
        Object.keys(foldersMap).length === 0 ? (
          <div className="text-center py-14 bg-slate-900/40 rounded-2xl border border-slate-800/80 space-y-3 px-4 v2s-animate-fade">
            <Folder className="w-9 h-9 text-slate-600 mx-auto" />
            <p className="text-sm text-slate-200 font-semibold">No media sources yet</p>
            <p className="text-xs text-slate-400">
              Add video/song links or load folders from your device.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 v2s-animate-fade">
            {Object.entries(foldersMap).map(([folderName, items]) => (
              <div
                key={folderName}
                className="p-4 sm:p-5 bg-slate-900/60 border border-slate-800 rounded-2xl hover:border-slate-700 transition-colors space-y-3"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2.5 bg-indigo-500/10 text-indigo-400 rounded-xl shrink-0">
                    <Folder className="w-5 h-5" />
                  </div>
                  <div className="min-w-0">
                    <h4 className="font-bold text-white text-sm truncate">{folderName}</h4>
                    <p className="text-xs text-slate-400 tabular-nums">{items.length} items</p>
                  </div>
                </div>

                <div className="space-y-1 pt-2 border-t border-slate-800/80">
                  {items.slice(0, 4).map((item) => (
                    <div
                      key={item.id}
                      onClick={() => handleOpenItem(item)}
                      className="flex items-center justify-between text-xs text-slate-300 hover:text-indigo-400 cursor-pointer p-2 rounded-lg hover:bg-slate-800/50 transition-colors gap-2"
                    >
                      <span className="truncate">{item.title}</span>
                      <span className="text-[11px] text-slate-500 font-mono tabular-nums shrink-0">
                        {formatDuration(item.duration)}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )
      )}

      {/* Playlists View */}
      {activeTab === 'playlists' && (
        <div className="space-y-4 v2s-animate-fade">
          <div className="flex flex-wrap justify-between items-center gap-3">
            <div>
              <h2 className="text-base sm:text-lg font-bold text-white">Playlists</h2>
              <p className="text-xs text-slate-400 tabular-nums">
                {playlists.length} saved playlists
              </p>
            </div>
            <div className="flex items-center gap-2 shrink-0">
              <button
                type="button"
                onClick={() => {
                  setPlaylistImportError(null);
                  setPlaylistAllowForce(false);
                  setShowImportPlaylistModal(true);
                }}
                className="min-h-[40px] px-3.5 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold flex items-center gap-1.5 shadow-md shadow-indigo-600/25 transition-colors whitespace-nowrap"
              >
                <Link2 className="w-3.5 h-3.5" />
                Add Playlist Link
              </button>
              <button
                type="button"
                onClick={() => setShowNewPlaylistModal(true)}
                className="min-h-[40px] px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-slate-200 border border-slate-800 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-colors whitespace-nowrap"
              >
                <Plus className="w-3.5 h-3.5" />
                New Playlist
              </button>
            </div>
          </div>

          {playlists.length === 0 ? (
            <div className="text-center py-14 bg-slate-900/35 rounded-2xl border border-slate-800/80 space-y-4 px-4">
              <div className="w-12 h-12 rounded-2xl bg-indigo-500/10 text-indigo-400 flex items-center justify-center mx-auto">
                <ListMusic className="w-6 h-6" />
              </div>
              <div className="space-y-1 max-w-md mx-auto">
                <p className="text-sm font-semibold text-white">No playlists yet</p>
                <p className="text-xs text-slate-400">
                  Import a YouTube, YouTube Music, Spotify, SoundCloud, or M3U playlist link — or build your own.
                </p>
              </div>
              <button
                type="button"
                onClick={() => setShowImportPlaylistModal(true)}
                className="min-h-[42px] px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold inline-flex items-center gap-1.5 transition-colors"
              >
                <Link2 className="w-3.5 h-3.5" />
                Add Playlist Link
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 items-start">
              {playlists.map((pl) => {
                const resolvedTracks = pl.itemIds
                  .map((id) => mediaItems.find((m) => m.id === id))
                  .filter((m): m is MediaItem => Boolean(m));
                const cover = pl.coverImage || resolvedTracks[0]?.thumbnail;
                const isExpanded = expandedPlaylistId === pl.id;

                return (
                  <div
                    key={pl.id}
                    className="p-4 bg-slate-900/65 border border-slate-800 rounded-2xl hover:border-slate-700 transition-colors space-y-3"
                  >
                    <div className="flex items-start justify-between gap-3">
                      <button
                        type="button"
                        onClick={() => setExpandedPlaylistId(isExpanded ? null : pl.id)}
                        className="flex items-center gap-3 min-w-0 text-left flex-1"
                      >
                        <div className="w-12 h-12 rounded-xl overflow-hidden shrink-0 border border-slate-800 bg-gradient-to-br from-indigo-900 to-purple-950 flex items-center justify-center text-indigo-300">
                          {cover ? (
                            <img
                              src={cover}
                              alt={pl.name}
                              referrerPolicy="no-referrer"
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <ListMusic className="w-5 h-5" />
                          )}
                        </div>
                        <div className="min-w-0">
                          <h4 className="font-bold text-white text-sm truncate">{pl.name}</h4>
                          <p className="text-xs text-slate-400 tabular-nums truncate">
                            {resolvedTracks.length} {resolvedTracks.length === 1 ? 'track' : 'tracks'}
                            {pl.description ? ` · ${pl.description}` : ''}
                          </p>
                        </div>
                      </button>

                      <div className="flex items-center gap-1 shrink-0">
                        {onPlayPlaylist && resolvedTracks.length > 0 && (
                          <button
                            type="button"
                            onClick={() => onPlayPlaylist(pl)}
                            className="min-h-[36px] px-3 py-1 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold flex items-center gap-1"
                          >
                            <Play className="w-3 h-3 fill-current" /> Play
                          </button>
                        )}
                        <button
                          type="button"
                          onClick={() => setExpandedPlaylistId(isExpanded ? null : pl.id)}
                          className="min-h-[36px] min-w-[36px] flex items-center justify-center rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
                          title={isExpanded ? 'Hide tracks' : 'View tracks'}
                        >
                          {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                        </button>
                        {onDeletePlaylist && (
                          <button
                            type="button"
                            onClick={() => onDeletePlaylist(pl.id)}
                            className="min-h-[36px] min-w-[36px] flex items-center justify-center rounded-lg text-slate-500 hover:text-red-400 hover:bg-slate-800 transition-colors"
                            title="Delete playlist"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        )}
                      </div>
                    </div>

                    {isExpanded && (
                      <div className="pt-2 border-t border-slate-800/80 space-y-1 max-h-64 overflow-y-auto pr-1">
                        {resolvedTracks.length === 0 ? (
                          <p className="text-xs text-slate-500 py-2 text-center">
                            No tracks yet. Tap + on any video or song to add it here.
                          </p>
                        ) : (
                          resolvedTracks.map((track, idx) => (
                            <div
                              key={track.id}
                              onClick={() =>
                                onPlayPlaylist ? onPlayPlaylist(pl, track) : handleOpenItem(track)
                              }
                              className="flex items-center justify-between gap-2 px-2.5 py-2 rounded-xl hover:bg-slate-800/70 cursor-pointer group transition-colors"
                            >
                              <div className="flex items-center gap-2.5 min-w-0">
                                <span className="text-[11px] font-mono text-slate-500 tabular-nums w-5 shrink-0">
                                  {idx + 1}.
                                </span>
                                <div className="min-w-0">
                                  <p className="text-xs font-semibold text-slate-200 group-hover:text-indigo-400 truncate">
                                    {track.title}
                                  </p>
                                  <p className="text-[11px] text-slate-500 truncate">
                                    {track.artist || track.sourceLabel} &middot;{' '}
                                    <span className="tabular-nums">{formatDuration(track.duration)}</span>
                                  </p>
                                </div>
                              </div>
                              {onRemoveFromPlaylist && (
                                <button
                                  type="button"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    onRemoveFromPlaylist(track.id, pl.id);
                                  }}
                                  className="min-h-[30px] min-w-[30px] flex items-center justify-center text-slate-500 hover:text-red-400 rounded-lg opacity-75 hover:opacity-100 shrink-0"
                                  title="Remove from playlist"
                                >
                                  <X className="w-3.5 h-3.5" />
                                </button>
                              )}
                            </div>
                          ))
                        )}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* Main Media Items */}
      {activeTab !== 'folders' && activeTab !== 'playlists' && (
        <div className="v2s-animate-fade">
          {sortedItems.length === 0 ? (
            <div className="text-center py-12 sm:py-16 px-5 bg-slate-900/35 rounded-2xl border border-slate-800/80 space-y-4">
              <div className="w-12 h-12 rounded-2xl bg-indigo-500/10 text-indigo-400 flex items-center justify-center mx-auto">
                <HardDrive className="w-6 h-6" />
              </div>
              <div className="space-y-1.5 max-w-md mx-auto">
                <p className="text-sm sm:text-base text-white font-semibold">
                  {mediaItems.length === 0
                    ? 'Nothing saved yet'
                    : 'No matching items'}
                </p>
                <p className="text-xs text-slate-400 leading-relaxed">
                  Paste any video, song, or playlist link above — or import files from device memory.
                </p>
              </div>
              <div className="flex flex-wrap items-center justify-center gap-3 pt-1">
                <button
                  type="button"
                  onClick={onOpenAddLink}
                  className="min-h-[42px] px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold flex items-center gap-1.5 transition-colors"
                >
                  <Link2 className="w-3.5 h-3.5" />
                  Add Link or Playlist
                </button>
                <button
                  type="button"
                  onClick={onOpenDeviceImport}
                  className="min-h-[42px] px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-200 border border-slate-800 text-xs font-semibold flex items-center gap-1.5 transition-colors"
                >
                  <FolderOpen className="w-3.5 h-3.5 text-emerald-400" />
                  Device Files
                </button>
              </div>
            </div>
          ) : viewMode === 'grid' ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
              {sortedItems.map((item) => {
                const isVid = item.type === 'video';
                const isShort = item.type === 'short';

                return (
                  <div
                    key={item.id}
                    className="group relative bg-slate-900/65 border border-slate-800/90 hover:border-slate-700 rounded-2xl overflow-hidden transition-transform duration-200 hover:-translate-y-0.5 flex flex-col"
                  >
                    <div
                      onClick={() => handleOpenItem(item)}
                      className="relative aspect-video bg-slate-950 overflow-hidden cursor-pointer"
                    >
                      {renderPoster(item)}

                      <div className="absolute inset-x-0 bottom-0 h-14 bg-gradient-to-t from-black/85 via-black/30 to-transparent pointer-events-none" />

                      <div className="absolute bottom-2.5 inset-x-3 flex items-center justify-between text-[11px] font-mono text-white/95 pointer-events-none">
                        <span className="flex items-center gap-1 font-semibold text-slate-200 capitalize">
                          {isVid && <Film className="w-3 h-3 text-indigo-400" />}
                          {isShort && <Flame className="w-3 h-3 text-orange-400" />}
                          {!isVid && !isShort && <Music className="w-3 h-3 text-emerald-400" />}
                          {item.type}
                        </span>
                        <span className="tabular-nums text-slate-200">
                          {formatDuration(item.duration)}
                        </span>
                      </div>

                      <div className="absolute inset-0 bg-black/35 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                        <div className="p-3 bg-indigo-600 rounded-full text-white shadow-xl scale-90 group-hover:scale-100 transition-transform">
                          <Play className="w-5 h-5 fill-current translate-x-0.5" />
                        </div>
                      </div>
                    </div>

                    <div className="p-3.5 sm:p-4 flex-1 flex flex-col justify-between space-y-2.5">
                      <div>
                        <h4
                          onClick={() => handleOpenItem(item)}
                          className="font-bold text-sm text-white hover:text-indigo-400 cursor-pointer truncate"
                          title={item.title}
                        >
                          {item.title}
                        </h4>
                        <p className="text-xs text-slate-400 truncate mt-0.5">
                          {item.artist || item.folder}
                          <span aria-hidden="true"> &middot; </span>
                          <span>{item.sourceLabel || item.size}</span>
                        </p>
                      </div>

                      <div className="flex items-center justify-end gap-0.5 pt-1.5 border-t border-slate-800/70">
                        {onAddToPlaylist && (
                          <button
                            type="button"
                            onClick={() => setPlaylistPickerItemId(item.id)}
                            className="min-h-[36px] min-w-[36px] flex items-center justify-center rounded-lg text-slate-400 hover:text-indigo-400 hover:bg-slate-800 transition-colors"
                            title="Add to Playlist"
                          >
                            <ListPlus className="w-3.5 h-3.5" />
                          </button>
                        )}
                        {item.sourceType === 'link' && (
                          <button
                            type="button"
                            onClick={() => onEditItem(item)}
                            className="min-h-[36px] min-w-[36px] flex items-center justify-center rounded-lg text-slate-400 hover:text-indigo-400 hover:bg-slate-800 transition-colors"
                            title="Edit Link"
                          >
                            <Pencil className="w-3.5 h-3.5" />
                          </button>
                        )}
                        <button
                          type="button"
                          onClick={() => onToggleFavorite(item.id)}
                          className={`min-h-[36px] min-w-[36px] flex items-center justify-center rounded-lg hover:bg-slate-800 transition-colors ${
                            item.isFavorite ? 'text-red-500' : 'text-slate-400 hover:text-white'
                          }`}
                          title="Favorite"
                        >
                          <Heart className={`w-3.5 h-3.5 ${item.isFavorite ? 'fill-current' : ''}`} />
                        </button>
                        <button
                          type="button"
                          onClick={() => onDeleteItem(item.id)}
                          className="min-h-[36px] min-w-[36px] flex items-center justify-center rounded-lg text-slate-400 hover:text-red-400 hover:bg-slate-800 transition-colors"
                          title="Remove"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="bg-slate-900/60 border border-slate-800 rounded-2xl overflow-hidden divide-y divide-slate-800/80">
              {sortedItems.map((item) => (
                <div
                  key={item.id}
                  className="p-3 sm:p-4 hover:bg-slate-800/50 flex items-center justify-between gap-3 transition-colors group cursor-pointer"
                  onClick={() => handleOpenItem(item)}
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="relative w-12 h-12 rounded-xl overflow-hidden shrink-0 bg-slate-950 border border-slate-800">
                      {renderPoster(item, true)}
                    </div>

                    <div className="min-w-0">
                      <h4 className="font-bold text-sm text-white group-hover:text-indigo-400 transition-colors truncate">
                        {item.title}
                      </h4>
                      <div className="flex items-center gap-2 text-xs text-slate-400 mt-0.5">
                        <span className="truncate">{item.artist || item.sourceLabel || item.folder}</span>
                        <span aria-hidden="true">&middot;</span>
                        <span className="capitalize">{item.type}</span>
                        <span aria-hidden="true">&middot;</span>
                        <span className="font-mono tabular-nums">{formatDuration(item.duration)}</span>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-1 shrink-0 text-xs text-slate-400">
                    {onAddToPlaylist && (
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setPlaylistPickerItemId(item.id);
                        }}
                        className="min-h-[38px] min-w-[38px] flex items-center justify-center rounded-lg text-slate-400 hover:text-indigo-400 hover:bg-slate-700 transition-colors"
                        title="Add to Playlist"
                      >
                        <ListPlus className="w-4 h-4" />
                      </button>
                    )}
                    {item.sourceType === 'link' && (
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          onEditItem(item);
                        }}
                        className="min-h-[38px] min-w-[38px] flex items-center justify-center rounded-lg text-slate-400 hover:text-indigo-400 hover:bg-slate-700 transition-colors"
                        title="Edit Link"
                      >
                        <Pencil className="w-4 h-4" />
                      </button>
                    )}
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        onToggleFavorite(item.id);
                      }}
                      className={`min-h-[38px] min-w-[38px] flex items-center justify-center rounded-lg hover:bg-slate-700 transition-colors ${
                        item.isFavorite ? 'text-red-500' : 'text-slate-400 hover:text-white'
                      }`}
                      title="Favorite"
                    >
                      <Heart className={`w-4 h-4 ${item.isFavorite ? 'fill-current' : ''}`} />
                    </button>
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        onDeleteItem(item.id);
                      }}
                      className="min-h-[38px] min-w-[38px] flex items-center justify-center rounded-lg text-slate-400 hover:text-red-400 hover:bg-slate-700 transition-colors"
                      title="Remove"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Add to Playlist Picker Modal */}
      {playlistPickerItemId && onAddToPlaylist && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md v2s-animate-fade">
          <div className="w-full max-w-sm bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4 shadow-2xl v2s-animate-modal">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-bold text-white">Add to Playlist</h3>
              <button
                type="button"
                onClick={() => setPlaylistPickerItemId(null)}
                className="min-h-[36px] min-w-[36px] flex items-center justify-center rounded-lg text-slate-400 hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
            {playlists.length === 0 ? (
              <div className="space-y-3 text-center py-3">
                <p className="text-xs text-slate-400">No playlists yet. Create one first.</p>
                <button
                  type="button"
                  onClick={() => {
                    setPlaylistPickerItemId(null);
                    setShowNewPlaylistModal(true);
                  }}
                  className="px-4 py-2 rounded-xl bg-indigo-600 text-white text-xs font-semibold"
                >
                  Create Playlist
                </button>
              </div>
            ) : (
              <div className="space-y-2 max-h-60 overflow-y-auto">
                {playlists.map((pl) => {
                  const alreadyIn = pl.itemIds.includes(playlistPickerItemId);
                  return (
                    <button
                      key={pl.id}
                      type="button"
                      onClick={() => {
                        onAddToPlaylist(playlistPickerItemId, pl.id);
                        setPlaylistPickerItemId(null);
                      }}
                      className="w-full p-3 rounded-xl bg-slate-950 hover:bg-slate-800 border border-slate-800 flex items-center justify-between text-xs text-white transition-colors"
                    >
                      <span className="font-semibold truncate">{pl.name}</span>
                      <span className="text-[11px] font-mono text-slate-400 shrink-0 tabular-nums">
                        {alreadyIn ? 'Added' : `${pl.itemIds.length} tracks`}
                      </span>
                    </button>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Import Playlist Link Modal */}
      {showImportPlaylistModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md v2s-animate-fade">
          <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4 shadow-2xl v2s-animate-modal">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-base font-bold text-white">Add Playlist Link</h3>
                <p className="text-xs text-slate-400">
                  YouTube, YouTube Music, Spotify, SoundCloud sets, or M3U playlist URLs
                </p>
              </div>
              <button
                type="button"
                onClick={() => setShowImportPlaylistModal(false)}
                className="min-h-[36px] min-w-[36px] flex items-center justify-center rounded-lg text-slate-400 hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form
              onSubmit={(e) => {
                e.preventDefault();
                submitPlaylistImport(false);
              }}
              className="space-y-3"
            >
              <input
                type="text"
                required
                autoFocus
                placeholder="https://youtube.com/playlist?list=... or .m3u"
                value={playlistLinkInput}
                onChange={(e) => {
                  setPlaylistLinkInput(e.target.value);
                  setPlaylistImportError(null);
                  setPlaylistAllowForce(false);
                }}
                className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white font-mono placeholder-slate-600 focus:outline-none focus:border-indigo-500"
              />
              <input
                type="text"
                placeholder="Playlist Name (auto-fetched if blank)"
                value={playlistTitleInput}
                onChange={(e) => setPlaylistTitleInput(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white placeholder-slate-600 focus:outline-none focus:border-indigo-500"
              />

              <div className="flex bg-slate-950 p-1 rounded-xl border border-slate-800 gap-1">
                {[
                  { id: 'video' as MediaType, label: 'Videos', icon: Film },
                  { id: 'song' as MediaType, label: 'Songs', icon: Music },
                ].map((opt) => {
                  const Icon = opt.icon;
                  const active = playlistTypeInput === opt.id;
                  return (
                    <button
                      key={opt.id}
                      type="button"
                      onClick={() => setPlaylistTypeInput(opt.id)}
                      className={`flex-1 py-2 text-xs font-semibold rounded-lg flex items-center justify-center gap-1.5 transition-colors ${
                        active ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-white'
                      }`}
                    >
                      <Icon className="w-3.5 h-3.5" />
                      {opt.label}
                    </button>
                  );
                })}
              </div>

              {playlistImportError && (
                <div className="rounded-xl bg-red-500/10 border border-red-500/30 p-3 text-xs text-red-200 space-y-2">
                  <div className="flex items-start gap-2">
                    <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
                    <span>{playlistImportError}</span>
                  </div>
                  {playlistAllowForce && (
                    <button
                      type="button"
                      onClick={() => submitPlaylistImport(true)}
                      className="px-3 py-1 rounded-lg bg-red-500/20 hover:bg-red-500/30 text-red-100 font-semibold"
                    >
                      Save Anyway
                    </button>
                  )}
                </div>
              )}

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowImportPlaylistModal(false)}
                  className="min-h-[40px] px-4 py-2 bg-slate-800 text-slate-300 rounded-lg text-xs font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={playlistImporting}
                  className="min-h-[40px] px-4 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-60 text-white rounded-lg text-xs font-semibold shadow-lg shadow-indigo-600/30"
                >
                  {playlistImporting ? 'Importing...' : 'Import & Play'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* New Playlist Modal */}
      {showNewPlaylistModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md v2s-animate-fade">
          <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4 shadow-2xl v2s-animate-modal">
            <h3 className="text-base font-bold text-white">Create New Playlist</h3>
            <form onSubmit={handleCreatePlaylistSubmit} className="space-y-3">
              <input
                type="text"
                required
                placeholder="Playlist Name"
                value={newPlaylistName}
                onChange={(e) => setNewPlaylistName(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-indigo-500"
              />
              <textarea
                placeholder="Description (Optional)"
                value={newPlaylistDesc}
                onChange={(e) => setNewPlaylistDesc(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-indigo-500 h-20 resize-none"
              />
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowNewPlaylistModal(false)}
                  className="min-h-[40px] px-4 py-2 bg-slate-800 text-slate-300 rounded-lg text-xs font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="min-h-[40px] px-4 py-2 bg-indigo-600 text-white rounded-lg text-xs font-semibold shadow-lg shadow-indigo-600/30"
                >
                  Create
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
