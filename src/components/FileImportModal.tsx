import React, { useRef, useState } from 'react';
import { UploadCloud, FileVideo, FileAudio, X, Plus, FolderOpen } from 'lucide-react';
import { MediaItem } from '../types/media';

interface FileImportModalProps {
  isOpen: boolean;
  onClose: () => void;
  onImportFiles: (items: MediaItem[]) => void;
}

export const FileImportModal: React.FC<FileImportModalProps> = ({
  isOpen,
  onClose,
  onImportFiles,
}) => {
  const [dragActive, setDragActive] = useState(false);
  const [importedList, setImportedList] = useState<MediaItem[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const folderInputRef = useRef<HTMLInputElement>(null);

  if (!isOpen) return null;

  const handleFiles = (files: FileList | null) => {
    if (!files || files.length === 0) return;

    const newItems: MediaItem[] = [];

    Array.from(files).forEach((file) => {
      const isVideo =
        file.type.startsWith('video/') ||
        /\.(mp4|mkv|webm|mov|avi|m4v|3gp)$/i.test(file.name);
      const isAudio =
        file.type.startsWith('audio/') ||
        /\.(mp3|wav|flac|aac|m4a|ogg|opus)$/i.test(file.name);

      if (!isVideo && !isAudio) return;

      const blobUrl = URL.createObjectURL(file);
      const sizeMb =
        file.size >= 1024 * 1024
          ? `${(file.size / (1024 * 1024)).toFixed(1)} MB`
          : `${Math.max(1, Math.round(file.size / 1024))} KB`;

      const relPath = (file as File & { webkitRelativePath?: string }).webkitRelativePath || '';
      const pathParts = relPath.split('/').filter(Boolean);
      const folderName =
        pathParts.length > 1 ? pathParts[pathParts.length - 2] : 'Device Memory';

      newItems.push({
        id: `device-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`,
        title: file.name.replace(/\.[^/.]+$/, ''),
        artist: folderName,
        duration: 0,
        uri: blobUrl,
        type: isVideo ? 'video' : 'song',
        folder: folderName,
        size: sizeMb,
        resolution: isVideo ? 'Device Video' : undefined,
        bitrate: isAudio ? 'Device Audio' : undefined,
        sourceType: 'device',
        sourceLabel: 'Device Memory',
        isFavorite: false,
        createdAt: Date.now(),
      });
    });

    if (newItems.length > 0) {
      setImportedList((prev) => [...prev, ...newItems]);
    }
  };

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFiles(e.dataTransfer.files);
    }
  };

  const handleConfirm = () => {
    if (importedList.length > 0) {
      onImportFiles(importedList);
      setImportedList([]);
    }
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md">
      <div className="relative w-full max-w-lg bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden flex flex-col">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/50">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-emerald-500/20 text-emerald-400 rounded-xl">
              <FolderOpen className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-white">Load Phone / Device Media</h2>
              <p className="text-xs text-slate-400">
                Select individual videos & songs or scan an entire device folder
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-4">
          <div
            onDragEnter={handleDrag}
            onDragLeave={handleDrag}
            onDragOver={handleDrag}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`border-2 border-dashed rounded-2xl p-6 text-center cursor-pointer transition-all ${
              dragActive
                ? 'border-indigo-500 bg-indigo-500/10'
                : 'border-slate-800 hover:border-slate-700 bg-slate-950/50'
            }`}
          >
            <input
              ref={fileInputRef}
              type="file"
              multiple
              accept="video/*,audio/*,.mkv,.mp4,.avi,.webm,.mov,.mp3,.wav,.flac,.aac,.m4a,.ogg"
              className="hidden"
              onChange={(e) => handleFiles(e.target.files)}
            />
            <input
              ref={folderInputRef}
              type="file"
              multiple
              // @ts-expect-error webkitdirectory is supported in Chromium/WebKit browsers
              webkitdirectory=""
              directory=""
              className="hidden"
              onChange={(e) => handleFiles(e.target.files)}
            />

            <div className="w-12 h-12 mx-auto mb-3 rounded-full bg-slate-800 text-indigo-400 flex items-center justify-center shadow-lg">
              <UploadCloud className="w-6 h-6" />
            </div>
            <h4 className="text-sm font-semibold text-white mb-1">
              Drop files here, or click to select videos & songs
            </h4>
            <p className="text-xs text-slate-400">
              Supports MP4, MKV, WebM, MOV, MP3, FLAC, WAV, AAC, M4A, OGG
            </p>
          </div>

          <button
            type="button"
            onClick={() => folderInputRef.current?.click()}
            className="w-full py-2.5 px-4 rounded-xl bg-slate-950 hover:bg-slate-800 border border-slate-800 text-xs font-semibold text-emerald-400 flex items-center justify-center gap-2 transition-colors"
          >
            <FolderOpen className="w-4 h-4" />
            Scan Entire Device / Phone Folder (Videos + Songs)
          </button>

          {importedList.length > 0 && (
            <div className="space-y-2">
              <div className="flex items-center justify-between text-xs text-slate-400">
                <span className="tabular-nums">{importedList.length} media files ready:</span>
                <button
                  onClick={() => setImportedList([])}
                  className="text-red-400 hover:underline"
                >
                  Clear all
                </button>
              </div>

              <div className="max-h-40 overflow-y-auto space-y-1.5 pr-1">
                {importedList.map((item) => (
                  <div
                    key={item.id}
                    className="flex items-center justify-between px-3 py-2 bg-slate-950 rounded-lg border border-slate-800 text-xs"
                  >
                    <div className="flex items-center gap-2 truncate">
                      {item.type === 'video' ? (
                        <FileVideo className="w-4 h-4 text-indigo-400 shrink-0" />
                      ) : (
                        <FileAudio className="w-4 h-4 text-emerald-400 shrink-0" />
                      )}
                      <span className="text-white truncate font-medium">{item.title}</span>
                    </div>
                    <span className="text-slate-500 shrink-0 font-mono ml-2 tabular-nums">{item.size}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="px-6 py-3 bg-slate-900 border-t border-slate-800 flex justify-between items-center text-xs">
          <span className="text-slate-500">Played directly from your device memory</span>
          <div className="flex gap-2">
            <button
              onClick={onClose}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              onClick={handleConfirm}
              disabled={importedList.length === 0}
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-semibold rounded-lg flex items-center gap-1.5 shadow-lg shadow-indigo-600/30 transition-all"
            >
              <Plus className="w-3.5 h-3.5" />
              Add {importedList.length > 0 ? `(${importedList.length})` : ''} to Library
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
