export type MediaType = 'video' | 'song' | 'short';

export interface MediaItem {
  id: string;
  title: string;
  artist?: string;
  album?: string;
  duration: number; // in seconds
  uri: string;
  type: MediaType;
  folder: string;
  size: string;
  resolution?: string;
  bitrate?: string;
  thumbnail?: string;
  isFavorite?: boolean;
  playedProgress?: number; // 0 to 1
  lastPlayed?: number;
  lyrics?: string[];
  genre?: string;
  year?: string;
  tags?: string[];
  likes?: number;
  commentsCount?: number;
  embedUrl?: string;
  extractedVideoUrl?: string;
  extractedAudioUrl?: string;
  sourceType?: 'link' | 'device';
  sourceLabel?: string;
  createdAt?: number;
}

export interface Playlist {
  id: string;
  name: string;
  description?: string;
  itemIds: string[];
  createdAt: number;
  updatedAt: number;
  coverImage?: string;
  sourceUrl?: string;
}

export interface EqualizerPreset {
  id: string;
  name: string;
  gains: number[]; // 10 bands (-12dB to +12dB)
}
