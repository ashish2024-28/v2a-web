import { MediaItem, Playlist, EqualizerPreset } from '../types/media';

export const INITIAL_MEDIA_ITEMS: MediaItem[] = [];

export const INITIAL_PLAYLISTS: Playlist[] = [];

export const EQUALIZER_PRESETS: EqualizerPreset[] = [
  { id: 'flat', name: 'Flat (Balanced)', gains: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0] },
  { id: 'bass', name: 'Bass Boost (Sub-Woofer)', gains: [7, 6, 4, 2, 0, 0, 0, 1, 2, 3] },
  { id: 'vocal', name: 'Vocal Booster (Crisp Speech)', gains: [-2, -1, 0, 3, 5, 5, 4, 2, 1, 0] },
  { id: 'rock', name: 'Rock & Heavy Metal', gains: [5, 4, 3, 1, -1, -1, 2, 4, 5, 5] },
  { id: 'pop', name: 'Pop & Commercial', gains: [2, 3, 4, 2, 0, 0, 2, 3, 4, 3] },
  { id: 'electronic', name: 'Electronic & EDM', gains: [6, 5, 3, 0, -2, 2, 1, 3, 5, 6] },
  { id: 'jazz', name: 'Jazz & Acoustic', gains: [3, 2, 1, 2, -1, -1, 1, 2, 4, 4] },
  { id: 'classical', name: 'Classical Orchestra', gains: [4, 3, 2, 1, -1, -1, 0, 2, 3, 4] },
  { id: 'hiphop', name: 'Hip-Hop 808s', gains: [6, 5, 4, 1, -1, -1, 1, 2, 3, 4] }
];

export const EQUALIZER_FREQUENCIES = [
  '31 Hz', '62 Hz', '125 Hz', '250 Hz', '500 Hz', '1 kHz', '2 kHz', '4 kHz', '8 kHz', '16 kHz'
];
