package com.example.data.repository

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.data.local.AudioMetadataDao
import com.example.data.local.AudioMetadataEntity
import com.example.data.local.PlaylistDao
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistItemEntity
import com.example.data.local.UserMediaLinkDao
import com.example.data.local.UserMediaLinkEntity
import com.example.data.local.VideoMetadataDao
import com.example.data.local.VideoMetadataEntity
import com.example.data.model.FolderItem
import com.example.data.model.PlaylistMediaType
import com.example.data.model.SongItem
import com.example.data.model.VideoItem
import com.example.util.MediaLinkUtils
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

sealed class MediaDeleteResult {
    data class Success(val deletedCount: Int) : MediaDeleteResult()
    data class RequiresPermission(
        val intentSender: IntentSender,
        val videoIds: List<Long> = emptyList(),
        val songIds: List<Long> = emptyList()
    ) : MediaDeleteResult()
    data class Failure(val message: String) : MediaDeleteResult()
}

class VideoRepository(
    private val context: Context,
    private val metadataDao: VideoMetadataDao,
    private val audioMetadataDao: AudioMetadataDao,
    private val playlistDao: PlaylistDao,
    private val userMediaLinkDao: UserMediaLinkDao
) {
    // Only real media discovered from device storage (internal + external)
    // plus locally saved user links. Zero default/sample media.
    private val _scannedVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val scannedVideos = _scannedVideos.asStateFlow()

    private val _scannedSongs = MutableStateFlow<List<SongItem>>(emptyList())
    val scannedSongs = _scannedSongs.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    // Reactive flow combining user-added links + device media scan + Room metadata
    val allVideosFlow: Flow<List<VideoItem>> = combine(
        _scannedVideos,
        metadataDao.getAllMetadataFlow(),
        userMediaLinkDao.getAllLinksFlow()
    ) { scannedList, metadataList, userLinks ->
        val metaMap = metadataList.associateBy { it.videoId }

        val linkedVideos = userLinks
            .filter { it.mediaType == "VIDEO" || it.mediaType == "SHORT" }
            .map { link ->
                val mediaId = MediaLinkUtils.toMediaItemId(link.id)
                val meta = metaMap[mediaId]
                val sourceFolder = link.sourceName.ifBlank { MediaLinkUtils.inferSourceName(link.url) }
                VideoItem(
                    id = mediaId,
                    uri = Uri.parse(link.url),
                    title = link.title,
                    displayName = link.title,
                    durationMs = 0L,
                    sizeBytes = 0L,
                    dateAddedSec = link.createdAtSec,
                    dateModifiedSec = link.createdAtSec,
                    width = if (link.mediaType == "SHORT") 1080 else 1920,
                    height = if (link.mediaType == "SHORT") 1920 else 1080,
                    mimeType = if (MediaLinkUtils.isDirectMediaUrl(link.url)) "video/*" else "text/html",
                    path = "Saved Links/${link.mediaType}/$sourceFolder/${link.title}",
                    isFavorite = meta?.isFavorite ?: false,
                    lastPositionMs = meta?.lastPositionMs ?: 0L,
                    lastPlayedTimestamp = meta?.lastPlayedTimestamp ?: 0L,
                    playCount = meta?.playCount ?: 0,
                    isDemoSample = false
                )
            }

        val deviceVideos = scannedList.map { video ->
            val meta = metaMap[video.id]
            if (meta != null) {
                video.copy(
                    isFavorite = meta.isFavorite,
                    lastPositionMs = meta.lastPositionMs,
                    lastPlayedTimestamp = meta.lastPlayedTimestamp,
                    playCount = meta.playCount
                )
            } else {
                video
            }
        }

        linkedVideos + deviceVideos
    }

    // Reactive audio list combining user-added song links + device audio scan + metadata
    val allSongsFlow: Flow<List<SongItem>> = combine(
        _scannedSongs,
        audioMetadataDao.getAllMetadataFlow(),
        userMediaLinkDao.getAllLinksFlow()
    ) { scannedList, metadataList, userLinks ->
        val metaMap = metadataList.associateBy { it.audioId }

        val linkedSongs = userLinks
            .filter { it.mediaType == "SONG" }
            .map { link ->
                val mediaId = MediaLinkUtils.toMediaItemId(link.id)
                val meta = metaMap[mediaId]
                val artistLabel = link.sourceName.ifBlank { MediaLinkUtils.inferSourceName(link.url) }
                SongItem(
                    id = mediaId,
                    uri = Uri.parse(link.url),
                    title = link.title,
                    artist = artistLabel,
                    album = "Saved Links",
                    durationMs = 0L,
                    sizeBytes = 0L,
                    dateAddedSec = link.createdAtSec,
                    path = "Saved Links/Songs/$artistLabel/${link.title}",
                    mimeType = if (MediaLinkUtils.isDirectMediaUrl(link.url)) "audio/*" else "text/html",
                    isFavorite = meta?.isFavorite ?: false,
                    lastPositionMs = meta?.lastPositionMs ?: 0L,
                    lastPlayedTimestamp = meta?.lastPlayedTimestamp ?: 0L,
                    isDemoSample = false
                )
            }

        val deviceSongs = scannedList.map { song ->
            val meta = metaMap[song.id]
            if (meta != null) {
                song.copy(
                    isFavorite = meta.isFavorite,
                    lastPositionMs = meta.lastPositionMs,
                    lastPlayedTimestamp = meta.lastPlayedTimestamp
                )
            } else {
                song
            }
        }

        linkedSongs + deviceSongs
    }

    // Folders reactive flow grouping videos by their storage location
    val foldersFlow: Flow<List<FolderItem>> = combine(allVideosFlow, allSongsFlow) { videos, songs ->
        val folderMap = mutableMapOf<String, MutableList<VideoItem>>()
        val songFolderMap = mutableMapOf<String, MutableList<SongItem>>()

        videos.forEach { video ->
            val folderKey = video.parentFolderName
            folderMap.getOrPut(folderKey) { mutableListOf() }.add(video)
        }

        songs.forEach { song ->
            val folderKey = if (song.path != null) {
                val segments = song.path.split("/").filter { it.isNotBlank() }
                if (segments.size >= 2) segments[segments.size - 2] else "Music"
            } else "Music"
            songFolderMap.getOrPut(folderKey) { mutableListOf() }.add(song)
        }

        val allFolderNames = (folderMap.keys + songFolderMap.keys).toSet()
        allFolderNames.map { name ->
            val vList = folderMap[name] ?: emptyList()
            val sList = songFolderMap[name] ?: emptyList()
            val totalBytes = vList.sumOf { it.sizeBytes } + sList.sumOf { it.sizeBytes }
            val latestTime = maxOf(
                vList.maxOfOrNull { it.dateAddedSec } ?: 0L,
                sList.maxOfOrNull { it.dateAddedSec } ?: 0L
            )
            val preview = vList.firstOrNull()?.uri ?: sList.firstOrNull()?.albumArtUri
            FolderItem(
                name = name,
                path = vList.firstOrNull()?.path ?: sList.firstOrNull()?.path ?: "/storage/emulated/0/$name",
                videoCount = vList.size,
                songCount = sList.size,
                totalSizeBytes = totalBytes,
                latestTimestamp = latestTime,
                previewUri = preview,
                previewVideo = vList.firstOrNull()
            )
        }.sortedByDescending { it.videoCount + it.songCount }
    }

    // Path fragments where Android/OEMs store preloaded UI / system sound effects.
    // Anything living under one of these directories is not user music.
    private val systemSoundPathMarkers = listOf(
        "/audio/ui/", "/audio/alarms/", "/audio/notifications/", "/audio/ringtones/",
        "/system/media/", "/product/media/", "/oem/media/", "/vendor/media/"
    )

    // Exact (case-insensitive) file/title names for Android's built-in UI sound
    // effects. Used as a defensive fallback when a device doesn't expose a path
    // for a given MediaStore row.
    private val systemSoundNames = setOf(
        "camera_click", "camera shutter", "chargingstarted", "dock", "dock_undock",
        "effect_tick", "incallnotification", "keypressdelete", "keypressinvalid",
        "keypressreturn", "keypressspacebar", "keypressstandard", "lock", "unlock",
        "lowbattery", "notificationsound", "ringtone", "screen_lock", "trusted",
        "videorecord", "video_stop", "wirelesschargingstarted", "wireless_charging_started"
    )

    private fun isSystemUiSound(path: String?, title: String?): Boolean {
        val lowerPath = path?.lowercase()
        if (lowerPath != null && systemSoundPathMarkers.any { lowerPath.contains(it) }) {
            return true
        }
        val normalizedTitle = title?.lowercase()?.replace(" ", "_")?.replace("-", "_")
        if (normalizedTitle != null && systemSoundNames.any { it.replace(" ", "_") == normalizedTitle }) {
            return true
        }
        return false
    }

    suspend fun scanMedia(@Suppress("UNUSED_PARAMETER") testingMode: Boolean = false): List<VideoItem> = withContext(Dispatchers.IO) {
        _isScanning.value = true
        val videoList = mutableListOf<VideoItem>()
        val songList = mutableListOf<SongItem>()
        val contentResolver: ContentResolver = context.contentResolver

        // Normal mode scans the user-accessible MediaStore external volume.
        // On modern Android this volume represents shared internal phone storage
        // (/storage/emulated/0) plus removable/shared external storage volumes.
        // Do NOT scan MediaStore.VOLUME_INTERNAL: that is the Android system
        // media database and can expose OS sounds/resources as user media.
        val videoUris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Query every shared external volume explicitly (primary + removable).
            MediaStore.getExternalVolumeNames(context).map {
                MediaStore.Video.Media.getContentUri(it)
            }.ifEmpty {
                listOf(MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL))
            }
        } else {
            listOf(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        }

        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATA
        )

        for (vUri in videoUris) {
            try {
                val cursor: Cursor? = contentResolver.query(
                    vUri,
                    videoProjection,
                    null,
                    null,
                    "${MediaStore.Video.Media.DATE_ADDED} DESC"
                )

                cursor?.use {
                    val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val nameCol = it.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                    val titleCol = it.getColumnIndex(MediaStore.Video.Media.TITLE)
                    val durCol = it.getColumnIndex(MediaStore.Video.Media.DURATION)
                    val sizeCol = it.getColumnIndex(MediaStore.Video.Media.SIZE)
                    val dateAddedCol = it.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                    val dateModCol = it.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                    val widthCol = it.getColumnIndex(MediaStore.Video.Media.WIDTH)
                    val heightCol = it.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                    val mimeCol = it.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                    val dataCol = it.getColumnIndex(MediaStore.Video.Media.DATA)

                    while (it.moveToNext()) {
                        val id = it.getLong(idCol)
                        val contentUri = ContentUris.withAppendedId(vUri, id)
                        val displayName = if (nameCol >= 0) it.getString(nameCol) ?: "Video_$id" else "Video_$id"
                        val title = if (titleCol >= 0) it.getString(titleCol) ?: displayName else displayName
                        val durationMs = if (durCol >= 0) it.getLong(durCol) else 0L
                        val size = if (sizeCol >= 0) it.getLong(sizeCol) else 0L
                        val dateAdded = if (dateAddedCol >= 0) it.getLong(dateAddedCol) else System.currentTimeMillis() / 1000
                        val dateMod = if (dateModCol >= 0) it.getLong(dateModCol) else dateAdded
                        val width = if (widthCol >= 0) it.getInt(widthCol) else 0
                        val height = if (heightCol >= 0) it.getInt(heightCol) else 0
                        val mime = if (mimeCol >= 0) it.getString(mimeCol) ?: "video/mp4" else "video/mp4"
                        val path = if (dataCol >= 0) it.getString(dataCol) else null

                        // Avoid duplicate IDs across collections
                        if (videoList.none { existing -> existing.id == id }) {
                            videoList.add(
                                VideoItem(
                                    id = id,
                                    uri = contentUri,
                                    title = title,
                                    displayName = displayName,
                                    durationMs = durationMs,
                                    sizeBytes = size,
                                    dateAddedSec = dateAdded,
                                    dateModifiedSec = dateMod,
                                    width = width,
                                    height = height,
                                    mimeType = mime,
                                    path = path
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoRepository", "Error querying Video MediaStore for $vUri", e)
            }
        }

        // 2. Scan all user-accessible audio from the same aggregate external volume.
        val audioUris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Query every shared external volume explicitly (primary + removable).
            MediaStore.getExternalVolumeNames(context).map {
                MediaStore.Audio.Media.getContentUri(it)
            }.ifEmpty {
                listOf(MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL))
            }
        } else {
            listOf(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        }

        val audioProjection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA
        )

        for (aUri in audioUris) {
            try {
                val audioCursor: Cursor? = contentResolver.query(
                    aUri,
                    audioProjection,
                    null,
                    null,
                    "${MediaStore.Audio.Media.TITLE} ASC"
                )

                audioCursor?.use {
                    val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleCol = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val artistCol = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val albumCol = it.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                    val durCol = it.getColumnIndex(MediaStore.Audio.Media.DURATION)
                    val sizeCol = it.getColumnIndex(MediaStore.Audio.Media.SIZE)
                    val dateCol = it.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                    val mimeCol = it.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                    val dataCol = it.getColumnIndex(MediaStore.Audio.Media.DATA)

                    while (it.moveToNext()) {
                        val id = it.getLong(idCol)
                        val contentUri = ContentUris.withAppendedId(aUri, id)
                        val title = if (titleCol >= 0) it.getString(titleCol) ?: "Audio $id" else "Audio $id"
                        val artist = if (artistCol >= 0) it.getString(artistCol) ?: "Unknown Artist" else "Unknown Artist"
                        val album = if (albumCol >= 0) it.getString(albumCol) ?: "Unknown Album" else "Unknown Album"
                        val durationMs = if (durCol >= 0) it.getLong(durCol) else 0L
                        val size = if (sizeCol >= 0) it.getLong(sizeCol) else 0L
                        val dateAdded = if (dateCol >= 0) it.getLong(dateCol) else System.currentTimeMillis() / 1000
                        val mimeType = if (mimeCol >= 0) it.getString(mimeCol).orEmpty() else ""
                        val path = if (dataCol >= 0) it.getString(dataCol) else null

                        if (isSystemUiSound(path, title)) {
                            continue
                        }

                        if (songList.none { existing -> existing.id == id }) {
                            songList.add(
                                SongItem(
                                    id = id,
                                    uri = contentUri,
                                    title = title,
                                    artist = artist,
                                    album = album,
                                    durationMs = durationMs,
                                    sizeBytes = size,
                                    dateAddedSec = dateAdded,
                                    path = path,
                                    mimeType = mimeType.ifBlank { "audio/*" }
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoRepository", "Error querying Audio MediaStore for $aUri", e)
            }
        }

        // 3. MediaStore.Files fallback for non-standard formats (AVI, FLV, WMV, TS, MKV, AMR, etc.)
        try {
            val filesUri = MediaStore.Files.getContentUri("external")
            val filesProjection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATA
            )
            val videoExts = setOf("mp4", "mkv", "avi", "mov", "flv", "wmv", "webm", "mpeg", "mpg", "m2v", "ts", "mts", "m2ts", "3gp", "3g2", "vob", "asf")
            val audioExts = setOf("mp3", "aac", "flac", "m4a", "wav", "ogg", "oga", "opus", "amr", "awb", "wma")

            val selection = "${MediaStore.Files.FileColumns.SIZE} > 1024"
            val fileCursor = contentResolver.query(
                filesUri,
                filesProjection,
                selection,
                null,
                "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
            )

            fileCursor?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val titleCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE)
                val sizeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)
                val dateModCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                val dataCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)

                while (cursor.moveToNext()) {
                    val path = if (dataCol >= 0) cursor.getString(dataCol) else null
                    val displayName = if (nameCol >= 0) cursor.getString(nameCol) ?: "" else ""
                    val ext = (path?.substringAfterLast('.', "") ?: displayName.substringAfterLast('.', "")).lowercase()
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(filesUri, id)
                    val title = if (titleCol >= 0) cursor.getString(titleCol) ?: displayName else displayName
                    val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                    val dateAdded = if (dateAddedCol >= 0) cursor.getLong(dateAddedCol) else System.currentTimeMillis() / 1000
                    val dateMod = if (dateModCol >= 0) cursor.getLong(dateModCol) else dateAdded
                    val mime = if (mimeCol >= 0) cursor.getString(mimeCol) ?: "" else ""

                    if (ext in videoExts) {
                        if (videoList.none { it.path == path || it.displayName == displayName }) {
                            videoList.add(
                                VideoItem(
                                    id = id,
                                    uri = contentUri,
                                    title = title.ifEmpty { displayName },
                                    displayName = displayName.ifEmpty { "Video_$id" },
                                    durationMs = 0L,
                                    sizeBytes = size,
                                    dateAddedSec = dateAdded,
                                    dateModifiedSec = dateMod,
                                    width = 0,
                                    height = 0,
                                    mimeType = if (mime.startsWith("video/")) mime else "video/$ext",
                                    path = path
                                )
                            )
                        }
                    } else if (ext in audioExts) {
                        if (!isSystemUiSound(path, title) && songList.none { it.path == path || it.title == title }) {
                            songList.add(
                                SongItem(
                                    id = id,
                                    uri = contentUri,
                                    title = title.ifEmpty { displayName },
                                    artist = "Unknown Artist",
                                    album = "Unknown Album",
                                    durationMs = 0L,
                                    sizeBytes = size,
                                    dateAddedSec = dateAdded,
                                    path = path,
                                    mimeType = if (mime.startsWith("audio/")) mime else "audio/*"
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error querying MediaStore.Files fallback", e)
        }

        val finalVideoList = videoList.distinctBy { it.uri.toString() }
        val finalSongList = songList.distinctBy { it.uri.toString() }

        _scannedVideos.value = finalVideoList
        _scannedSongs.value = finalSongList
        _isScanning.value = false
        finalVideoList
    }

    suspend fun addUserMediaLink(
        rawUrl: String,
        title: String?,
        mediaType: String,
        sourceName: String?
    ): UserMediaLinkEntity = withContext(Dispatchers.IO) {
        val normalizedUrl = MediaLinkUtils.normalizeUrl(rawUrl)
        val cleanType = when (mediaType.uppercase()) {
            "SHORT", "SHORTS" -> "SHORT"
            "SONG", "AUDIO", "MUSIC" -> "SONG"
            else -> "VIDEO"
        }

        val existing = userMediaLinkDao.findByUrl(normalizedUrl)
        if (existing != null) {
            return@withContext existing
        }

        val needsLookup = title.isNullOrBlank() || sourceName.isNullOrBlank()
        val oembed = if (needsLookup && normalizedUrl.startsWith("http", ignoreCase = true)) {
            MediaLinkUtils.fetchOEmbedMetadata(normalizedUrl)
        } else {
            null
        }

        val resolvedTitle = title?.trim()?.takeIf { it.isNotBlank() }
            ?: oembed?.title?.takeIf { oembed.status == "ok" && it.isNotBlank() }
            ?: MediaLinkUtils.inferDefaultTitle(normalizedUrl, cleanType)
        val resolvedSource = sourceName?.trim()?.takeIf { it.isNotBlank() }
            ?: oembed?.author?.takeIf { oembed.status == "ok" && it.isNotBlank() }
            ?: MediaLinkUtils.inferSourceName(normalizedUrl)
        val thumb = oembed?.thumbnailUrl?.takeIf { it.isNotBlank() }
            ?: MediaLinkUtils.getYouTubeThumbnailUrl(normalizedUrl)

        val entity = UserMediaLinkEntity(
            url = normalizedUrl,
            title = resolvedTitle,
            mediaType = cleanType,
            sourceName = resolvedSource,
            thumbnailUrl = thumb,
            createdAtSec = System.currentTimeMillis() / 1000L
        )
        val insertedId = userMediaLinkDao.insertLink(entity)
        entity.copy(id = insertedId)
    }

    suspend fun updateUserMediaLink(
        mediaId: Long,
        rawUrl: String,
        title: String?,
        mediaType: String,
        sourceName: String?
    ): UserMediaLinkEntity? = withContext(Dispatchers.IO) {
        if (!MediaLinkUtils.isUserLinkId(mediaId)) return@withContext null
        val entityId = MediaLinkUtils.toEntityId(mediaId)
        val existing = userMediaLinkDao.getLinkById(entityId) ?: return@withContext null

        val normalizedUrl = MediaLinkUtils.normalizeUrl(rawUrl)
        val cleanType = when (mediaType.uppercase()) {
            "SHORT", "SHORTS" -> "SHORT"
            "SONG", "AUDIO", "MUSIC" -> "SONG"
            else -> "VIDEO"
        }
        val needsLookup = title.isNullOrBlank() || sourceName.isNullOrBlank()
        val oembed = if (needsLookup && normalizedUrl.startsWith("http", ignoreCase = true)) {
            MediaLinkUtils.fetchOEmbedMetadata(normalizedUrl)
        } else {
            null
        }

        val resolvedTitle = title?.trim()?.takeIf { it.isNotBlank() }
            ?: oembed?.title?.takeIf { oembed.status == "ok" && it.isNotBlank() }
            ?: MediaLinkUtils.inferDefaultTitle(normalizedUrl, cleanType)
        val resolvedSource = sourceName?.trim()?.takeIf { it.isNotBlank() }
            ?: oembed?.author?.takeIf { oembed.status == "ok" && it.isNotBlank() }
            ?: MediaLinkUtils.inferSourceName(normalizedUrl)
        val thumb = oembed?.thumbnailUrl?.takeIf { it.isNotBlank() }
            ?: MediaLinkUtils.getYouTubeThumbnailUrl(normalizedUrl)
            ?: existing.thumbnailUrl

        userMediaLinkDao.updateLink(
            id = entityId,
            url = normalizedUrl,
            title = resolvedTitle,
            mediaType = cleanType,
            sourceName = resolvedSource,
            thumbnailUrl = thumb
        )
        existing.copy(
            url = normalizedUrl,
            title = resolvedTitle,
            mediaType = cleanType,
            sourceName = resolvedSource,
            thumbnailUrl = thumb
        )
    }

    suspend fun deleteUserMediaLinksByMediaIds(mediaIds: List<Long>) = withContext(Dispatchers.IO) {
        val entityIds = mediaIds.filter { MediaLinkUtils.isUserLinkId(it) }.map { MediaLinkUtils.toEntityId(it) }
        if (entityIds.isNotEmpty()) {
            userMediaLinkDao.deleteLinksByIds(entityIds)
        }
    }

    suspend fun toggleFavorite(videoId: Long, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        val newStatus = !currentStatus
        val existing = metadataDao.getMetadata(videoId)
        if (existing != null) {
            metadataDao.setFavorite(videoId, newStatus)
        } else {
            metadataDao.upsert(
                VideoMetadataEntity(
                    videoId = videoId,
                    isFavorite = newStatus
                )
            )
        }
    }

    suspend fun recordPlaybackStart(videoId: Long, positionMs: Long = 0L) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val existing = metadataDao.getMetadata(videoId)
        if (existing != null) {
            metadataDao.recordPlaybackStart(videoId, positionMs, now)
        } else {
            metadataDao.upsert(
                VideoMetadataEntity(
                    videoId = videoId,
                    lastPositionMs = positionMs,
                    lastPlayedTimestamp = now,
                    playCount = 1
                )
            )
        }
    }

    suspend fun updatePlaybackPosition(videoId: Long, positionMs: Long) = withContext(Dispatchers.IO) {
        metadataDao.updateProgress(videoId, positionMs, System.currentTimeMillis())
    }

    suspend fun recordSongPlaybackStart(songId: Long, positionMs: Long = 0L) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val existing = audioMetadataDao.getMetadata(songId)
        if (existing != null) {
            audioMetadataDao.recordPlaybackStart(songId, positionMs, now)
        } else {
            audioMetadataDao.upsert(
                AudioMetadataEntity(
                    audioId = songId,
                    lastPositionMs = positionMs,
                    lastPlayedTimestamp = now,
                    playCount = 1
                )
            )
        }
    }

    suspend fun updateSongPlaybackPosition(songId: Long, positionMs: Long) = withContext(Dispatchers.IO) {
        audioMetadataDao.updateProgress(songId, positionMs, System.currentTimeMillis())
    }

    suspend fun toggleSongFavorite(songId: Long, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        val newStatus = !currentStatus
        val existing = audioMetadataDao.getMetadata(songId)
        if (existing != null) {
            audioMetadataDao.setFavorite(songId, newStatus)
        } else {
            audioMetadataDao.upsert(AudioMetadataEntity(audioId = songId, isFavorite = newStatus))
        }
    }

    suspend fun clearSongHistory(songId: Long) = withContext(Dispatchers.IO) {
        audioMetadataDao.clearSongHistory(songId)
    }

    suspend fun clearAllSongHistory() = withContext(Dispatchers.IO) {
        audioMetadataDao.clearAllSongHistory()
    }

    suspend fun clearVideoHistory(videoId: Long) = withContext(Dispatchers.IO) {
        metadataDao.clearVideoHistory(videoId)
    }

    suspend fun clearAllVideoHistory() = withContext(Dispatchers.IO) {
        metadataDao.clearAllVideoHistory()
    }

    suspend fun cleanupDeletedVideos(videoIds: List<Long>) = withContext(Dispatchers.IO) {
        if (videoIds.isEmpty()) return@withContext
        val idSet = videoIds.toSet()
        _scannedVideos.value = _scannedVideos.value.filter { it.id !in idSet }
        try {
            metadataDao.deleteMultipleMetadata(videoIds)
            playlistDao.removeAllItemsWithMediaIds(videoIds)
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error cleaning up DB metadata for videos", e)
        }
    }

    suspend fun cleanupDeletedSongs(songIds: List<Long>) = withContext(Dispatchers.IO) {
        if (songIds.isEmpty()) return@withContext
        val idSet = songIds.toSet()
        _scannedSongs.value = _scannedSongs.value.filter { it.id !in idSet }
        try {
            audioMetadataDao.deleteMultipleMetadata(songIds)
            playlistDao.removeAllItemsWithMediaIds(songIds)
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error cleaning up DB metadata for songs", e)
        }
    }

    suspend fun requestDeleteVideos(videos: List<VideoItem>): MediaDeleteResult = withContext(Dispatchers.IO) {
        if (videos.isEmpty()) return@withContext MediaDeleteResult.Success(0)

        val userLinkVideos = videos.filter { MediaLinkUtils.isUserLinkId(it.id) }
        val demoOrDirectVideos = videos.filter { !MediaLinkUtils.isUserLinkId(it.id) && (it.isDemoSample || it.uri.scheme == "file") }
        val mediaStoreVideos = videos.filter { !MediaLinkUtils.isUserLinkId(it.id) && !it.isDemoSample && it.uri.scheme != "file" }

        if (userLinkVideos.isNotEmpty()) {
            val linkMediaIds = userLinkVideos.map { it.id }
            deleteUserMediaLinksByMediaIds(linkMediaIds)
            cleanupDeletedVideos(linkMediaIds)
        }

        // Clean direct file items immediately
        if (demoOrDirectVideos.isNotEmpty()) {
            val demoIds = demoOrDirectVideos.map { it.id }
            cleanupDeletedVideos(demoIds)
            for (v in demoOrDirectVideos) {
                try {
                    if (!v.path.isNullOrBlank()) {
                        val file = File(v.path)
                        if (file.exists()) file.delete()
                    }
                } catch (e: Exception) {
                    Log.e("VideoRepository", "Direct file delete failed: ${v.path}", e)
                }
            }
        }

        if (mediaStoreVideos.isEmpty()) {
            return@withContext MediaDeleteResult.Success(userLinkVideos.size + demoOrDirectVideos.size)
        }

        // On Android 11+ (API 30+), use createDeleteRequest to prompt system dialog
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val uris = mediaStoreVideos.map { it.uri }
                val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
                return@withContext MediaDeleteResult.RequiresPermission(
                    intentSender = pendingIntent.intentSender,
                    videoIds = mediaStoreVideos.map { it.id },
                    songIds = emptyList()
                )
            } catch (e: Exception) {
                Log.e("VideoRepository", "Failed to create MediaStore delete request for videos", e)
            }
        }

        // Fallback for Android 10 (Q) or <= 29
        val deletedIds = mutableListOf<Long>()
        var recoverableException: RecoverableSecurityException? = null

        for (video in mediaStoreVideos) {
            try {
                val rows = context.contentResolver.delete(video.uri, null, null)
                if (rows > 0) {
                    deletedIds.add(video.id)
                }
                if (!video.path.isNullOrBlank()) {
                    try {
                        val f = File(video.path)
                        if (f.exists()) f.delete()
                    } catch (_: Exception) {}
                }
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                    recoverableException = e
                }
            } catch (e: Exception) {
                Log.e("VideoRepository", "Error deleting video ${video.id}", e)
            }
        }

        if (deletedIds.isNotEmpty()) {
            cleanupDeletedVideos(deletedIds)
        }

        if (recoverableException != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return@withContext MediaDeleteResult.RequiresPermission(
                intentSender = recoverableException.userAction.actionIntent.intentSender,
                videoIds = mediaStoreVideos.map { it.id },
                songIds = emptyList()
            )
        }

        val total = demoOrDirectVideos.size + deletedIds.size
        return@withContext MediaDeleteResult.Success(total)
    }

    suspend fun requestDeleteSongs(songs: List<SongItem>): MediaDeleteResult = withContext(Dispatchers.IO) {
        if (songs.isEmpty()) return@withContext MediaDeleteResult.Success(0)

        val userLinkSongs = songs.filter { MediaLinkUtils.isUserLinkId(it.id) }
        val demoOrDirectSongs = songs.filter { !MediaLinkUtils.isUserLinkId(it.id) && it.uri.scheme == "file" }
        val mediaStoreSongs = songs.filter { !MediaLinkUtils.isUserLinkId(it.id) && it.uri.scheme != "file" }

        if (userLinkSongs.isNotEmpty()) {
            val linkMediaIds = userLinkSongs.map { it.id }
            deleteUserMediaLinksByMediaIds(linkMediaIds)
            cleanupDeletedSongs(linkMediaIds)
        }

        // Clean direct file items immediately
        if (demoOrDirectSongs.isNotEmpty()) {
            val directIds = demoOrDirectSongs.map { it.id }
            cleanupDeletedSongs(directIds)
            for (s in demoOrDirectSongs) {
                try {
                    if (!s.path.isNullOrBlank()) {
                        val file = File(s.path)
                        if (file.exists()) file.delete()
                    }
                } catch (e: Exception) {
                    Log.e("VideoRepository", "Direct file delete failed: ${s.path}", e)
                }
            }
        }

        if (mediaStoreSongs.isEmpty()) {
            return@withContext MediaDeleteResult.Success(userLinkSongs.size + demoOrDirectSongs.size)
        }

        // On Android 11+ (API 30+), use createDeleteRequest to prompt system dialog
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val uris = mediaStoreSongs.map { it.uri }
                val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
                return@withContext MediaDeleteResult.RequiresPermission(
                    intentSender = pendingIntent.intentSender,
                    videoIds = emptyList(),
                    songIds = mediaStoreSongs.map { it.id }
                )
            } catch (e: Exception) {
                Log.e("VideoRepository", "Failed to create MediaStore delete request for songs", e)
            }
        }

        // Fallback for Android 10 (Q) or <= 29
        val deletedIds = mutableListOf<Long>()
        var recoverableException: RecoverableSecurityException? = null

        for (song in mediaStoreSongs) {
            try {
                val rows = context.contentResolver.delete(song.uri, null, null)
                if (rows > 0) {
                    deletedIds.add(song.id)
                }
                if (!song.path.isNullOrBlank()) {
                    try {
                        val f = File(song.path)
                        if (f.exists()) f.delete()
                    } catch (_: Exception) {}
                }
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                    recoverableException = e
                }
            } catch (e: Exception) {
                Log.e("VideoRepository", "Error deleting song ${song.id}", e)
            }
        }

        if (deletedIds.isNotEmpty()) {
            cleanupDeletedSongs(deletedIds)
        }

        if (recoverableException != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return@withContext MediaDeleteResult.RequiresPermission(
                intentSender = recoverableException.userAction.actionIntent.intentSender,
                videoIds = emptyList(),
                songIds = mediaStoreSongs.map { it.id }
            )
        }

        val total = demoOrDirectSongs.size + deletedIds.size
        return@withContext MediaDeleteResult.Success(total)
    }

    suspend fun deleteVideo(video: VideoItem): Boolean = withContext(Dispatchers.IO) {
        val result = requestDeleteVideos(listOf(video))
        result is MediaDeleteResult.Success && result.deletedCount > 0
    }

    suspend fun deleteMultipleVideos(videos: List<VideoItem>): Int = withContext(Dispatchers.IO) {
        val result = requestDeleteVideos(videos)
        if (result is MediaDeleteResult.Success) result.deletedCount else 0
    }

    suspend fun deleteSong(song: SongItem): Boolean = withContext(Dispatchers.IO) {
        val result = requestDeleteSongs(listOf(song))
        result is MediaDeleteResult.Success && result.deletedCount > 0
    }

    suspend fun deleteMultipleSongs(songs: List<SongItem>): Int = withContext(Dispatchers.IO) {
        val result = requestDeleteSongs(songs)
        if (result is MediaDeleteResult.Success) result.deletedCount else 0
    }

    fun shareVideo(video: VideoItem) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = video.mimeType.ifBlank { "video/*" }
                putExtra(Intent.EXTRA_STREAM, video.uri)
                putExtra(Intent.EXTRA_SUBJECT, video.title)
                putExtra(Intent.EXTRA_TEXT, "Watch ${video.title} on V2S")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Video").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error sharing video", e)
        }
    }

    fun shareMultipleVideos(videos: List<VideoItem>) {
        if (videos.isEmpty()) return
        if (videos.size == 1) {
            shareVideo(videos.first())
            return
        }
        try {
            val uris = ArrayList(videos.map { it.uri })
            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "video/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                putExtra(Intent.EXTRA_SUBJECT, "Sharing ${videos.size} videos from V2S")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share ${videos.size} Videos").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error sharing multiple videos", e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PLAYLIST MANAGEMENT
    // ─────────────────────────────────────────────────────────────────────────

    val customPlaylistsFlow: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylistsFlow()
    val playlistItemsFlow: Flow<List<PlaylistItemEntity>> = playlistDao.getAllPlaylistItemsFlow()

    suspend fun createPlaylist(title: String, category: String = "ALL"): String = withContext(Dispatchers.IO) {
        val cat = when (category.uppercase()) {
            "SONGS", "SONG", "AUDIO" -> "SONGS"
            "VIDEOS", "VIDEO" -> "VIDEOS"
            else -> "ALL"
        }
        val prefix = when (cat) {
            "SONGS" -> "pl_songs_"
            "VIDEOS" -> "pl_videos_"
            else -> "pl_"
        }
        val id = "$prefix${System.currentTimeMillis()}"
        playlistDao.insertPlaylist(
            PlaylistEntity(
                id = id,
                title = title.trim(),
                isCustom = true,
                category = cat,
                createdAt = System.currentTimeMillis()
            )
        )
        id
    }

    suspend fun deletePlaylist(playlistId: String) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
        playlistDao.clearPlaylistItems(playlistId)
    }

    suspend fun renamePlaylist(playlistId: String, newTitle: String) = withContext(Dispatchers.IO) {
        playlistDao.renamePlaylist(playlistId, newTitle.trim())
    }

    suspend fun addItemToPlaylist(playlistId: String, mediaType: PlaylistMediaType, mediaId: Long) = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylistItem(
            PlaylistItemEntity(
                playlistId = playlistId,
                mediaType = mediaType.name,
                mediaId = mediaId,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeItemFromPlaylist(playlistId: String, mediaType: PlaylistMediaType, mediaId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeItem(playlistId, mediaType.name, mediaId)
    }

    fun shareSong(song: SongItem) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, song.uri)
                putExtra(Intent.EXTRA_SUBJECT, song.title)
                putExtra(Intent.EXTRA_TEXT, "Listen to ${song.title} - ${song.artist} on V2S")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Song").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error sharing song", e)
        }
    }

    fun shareMultipleSongs(songs: List<SongItem>) {
        if (songs.isEmpty()) return
        if (songs.size == 1) {
            shareSong(songs.first())
            return
        }
        try {
            val uris = ArrayList(songs.map { it.uri })
            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "audio/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                putExtra(Intent.EXTRA_SUBJECT, "Sharing ${songs.size} songs from V2S")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share ${songs.size} Songs").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error sharing multiple songs", e)
        }
    }

    suspend fun addMultipleItemsToPlaylist(playlistId: String, items: List<Pair<PlaylistMediaType, Long>>) = withContext(Dispatchers.IO) {
        items.forEach { (type, mediaId) ->
            playlistDao.insertPlaylistItem(
                PlaylistItemEntity(
                    playlistId = playlistId,
                    mediaType = type.name,
                    mediaId = mediaId,
                    addedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun shareMedia(uri: Uri, mimeType: String, title: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType.ifBlank { "*/*" }
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Listen/Watch $title on V2S")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Media").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error sharing media", e)
        }
    }
}