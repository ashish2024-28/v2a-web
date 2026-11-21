package com.example.util

import android.net.Uri
import android.util.Patterns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class UrlValidationResult(
    val valid: Boolean,
    val normalizedUrl: String,
    val errorMessage: String? = null
)

data class OEmbedMetadata(
    val status: String, // "ok", "not_found", "embed_restricted", "unavailable"
    val title: String? = null,
    val author: String? = null,
    val thumbnailUrl: String? = null
)

data class ExtractedMediaStream(
    val streamUrl: String,
    val audioStreamUrl: String? = null,
    val title: String? = null,
    val author: String? = null,
    val thumbnailUrl: String? = null,
    val mimeType: String? = null
)

object MediaLinkUtils {

    const val USER_LINK_ID_OFFSET = 100_000L

    fun toMediaItemId(entityId: Long): Long = -(USER_LINK_ID_OFFSET + entityId)

    fun isUserLinkId(mediaId: Long): Boolean = mediaId <= -USER_LINK_ID_OFFSET

    fun toEntityId(mediaId: Long): Long = (-mediaId) - USER_LINK_ID_OFFSET

    fun normalizeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return ""
        return if (
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("rtsp://", ignoreCase = true) ||
            trimmed.startsWith("rtmp://", ignoreCase = true)
        ) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    fun validateMediaUrl(rawUrl: String): UrlValidationResult {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) {
            return UrlValidationResult(false, "", "Please paste a video or song link.")
        }
        if (trimmed.any { it.isWhitespace() }) {
            return UrlValidationResult(
                false,
                trimmed,
                "Link contains spaces. Paste a single valid URL without spaces."
            )
        }
        val lowerRaw = trimmed.lowercase(Locale.ROOT)
        if (
            lowerRaw.startsWith("javascript:") ||
            lowerRaw.startsWith("data:") ||
            lowerRaw.startsWith("vbscript:") ||
            lowerRaw.startsWith("file:")
        ) {
            return UrlValidationResult(
                false,
                trimmed,
                "Unsupported link type. Use http://, https://, rtsp://, or rtmp://."
            )
        }

        val normalized = normalizeUrl(trimmed)
        val uri = runCatching { Uri.parse(normalized) }.getOrNull()
        val host = uri?.host?.lowercase(Locale.ROOT).orEmpty()
        if (host.isBlank()) {
            return UrlValidationResult(
                false,
                normalized,
                "Invalid link format. Example: https://youtube.com/watch?v=... or .mp4 / .mp3"
            )
        }

        val isIpOrLocal = host == "localhost" || Regex("""^\d{1,3}(\.\d{1,3}){3}$""").matches(host)
        val hasTld = host.contains('.') && !host.startsWith('.') && !host.endsWith('.') &&
            host.substringAfterLast('.').length >= 2
        val matchesWebPattern = normalized.startsWith("rtsp://", true) ||
            normalized.startsWith("rtmp://", true) ||
            Patterns.WEB_URL.matcher(normalized).matches()

        if ((!isIpOrLocal && !hasTld) || !matchesWebPattern) {
            return UrlValidationResult(
                false,
                normalized,
                "Enter a valid link domain (e.g. youtube.com, vimeo.com, or example.com/video.mp4)."
            )
        }

        val pathSegments = uri?.pathSegments ?: emptyList()

        if (host == "youtu.be" || host.endsWith(".youtu.be")) {
            if (extractYouTubeVideoId(normalized) == null) {
                return UrlValidationResult(
                    false,
                    normalized,
                    "Incomplete youtu.be link. Add the 11-character video ID."
                )
            }
        } else if (host == "youtube.com" || host.endsWith(".youtube.com")) {
            if (extractYouTubeVideoId(normalized) == null) {
                return UrlValidationResult(
                    false,
                    normalized,
                    "Incomplete YouTube link. Paste a specific video, Shorts, or Music track link."
                )
            }
        }

        if (host == "vimeo.com" || host.endsWith(".vimeo.com")) {
            if (extractVimeoId(normalized) == null) {
                return UrlValidationResult(
                    false,
                    normalized,
                    "Incomplete Vimeo link. Include the numeric video ID (e.g. vimeo.com/76979871)."
                )
            }
        }

        if (host == "open.spotify.com") {
            val validSpotify = Regex("""^/(intl-[a-z-]+/)?(track|album|playlist|episode|show)/[A-Za-z0-9]+""", RegexOption.IGNORE_CASE)
                .containsMatchIn(uri?.path.orEmpty())
            if (!validSpotify) {
                return UrlValidationResult(
                    false,
                    normalized,
                    "Incomplete Spotify link. Paste a specific track, album, or playlist link."
                )
            }
        }

        if ((host == "soundcloud.com" || host == "www.soundcloud.com") && pathSegments.size < 2) {
            return UrlValidationResult(
                false,
                normalized,
                "Incomplete SoundCloud link. Include artist and track name."
            )
        }

        if ((host.endsWith("instagram.com") || host.endsWith("tiktok.com") || host.endsWith("facebook.com")) &&
            pathSegments.isEmpty() &&
            !normalized.contains("?v=", ignoreCase = true)
        ) {
            return UrlValidationResult(
                false,
                normalized,
                "Paste a direct video or reel link instead of the website homepage."
            )
        }

        return UrlValidationResult(true, normalized)
    }

    suspend fun fetchOEmbedMetadata(rawUrl: String): OEmbedMetadata = withContext(Dispatchers.IO) {
        val normalized = normalizeUrl(rawUrl)
        val ytId = extractYouTubeVideoId(normalized)
        val endpoint = if (ytId != null) {
            val canonical = "https://www.youtube.com/watch?v=$ytId"
            "https://www.youtube.com/oembed?url=${URLEncoder.encode(canonical, "UTF-8")}&format=json"
        } else {
            "https://noembed.com/embed?url=${URLEncoder.encode(normalized, "UTF-8")}"
        }

        var conn: HttpURLConnection? = null
        try {
            conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("Accept", "application/json")
            }
            val code = conn.responseCode
            if (code == HttpURLConnection.HTTP_NOT_FOUND || code == HttpURLConnection.HTTP_BAD_REQUEST) {
                return@withContext OEmbedMetadata(status = "not_found")
            }
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED || code == HttpURLConnection.HTTP_FORBIDDEN) {
                return@withContext OEmbedMetadata(status = "embed_restricted")
            }
            if (code in 200..299) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                if (json.has("error")) {
                    val err = json.optString("error").lowercase(Locale.ROOT)
                    return@withContext when {
                        err.contains("404") || err.contains("not found") -> OEmbedMetadata(status = "not_found")
                        err.contains("401") || err.contains("forbidden") -> OEmbedMetadata(status = "embed_restricted")
                        else -> OEmbedMetadata(status = "unavailable")
                    }
                }
                return@withContext OEmbedMetadata(
                    status = "ok",
                    title = json.optString("title").takeIf { it.isNotBlank() },
                    author = json.optString("author_name").takeIf { it.isNotBlank() },
                    thumbnailUrl = json.optString("thumbnail_url").takeIf { it.isNotBlank() }
                )
            }
            OEmbedMetadata(status = "unavailable")
        } catch (_: Exception) {
            OEmbedMetadata(status = "unavailable")
        } finally {
            conn?.disconnect()
        }
    }

    fun extractYouTubeVideoId(rawUrl: String): String? {
        val url = rawUrl.trim()
        if (url.isEmpty()) return null

        val patterns = listOf(
            Regex("""(?:youtube\.com/watch\?(?:.*&)?v=|youtube\.com/embed/|youtube\.com/v/|youtube\.com/shorts/|youtube\.com/live/|music\.youtube\.com/watch\?(?:.*&)?v=|youtu\.be/)([A-Za-z0-9_-]{11})(?:[?&#/]|$)""", RegexOption.IGNORE_CASE)
        )
        for (regex in patterns) {
            val match = regex.find(url)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    fun isYouTubeUrl(url: String): Boolean {
        val lower = url.lowercase(Locale.ROOT)
        return lower.contains("youtube.com") || lower.contains("youtu.be")
    }

    fun isYouTubeShortsUrl(url: String): Boolean {
        return url.lowercase(Locale.ROOT).contains("/shorts/")
    }

    fun getYouTubeThumbnailUrl(url: String): String? {
        val id = extractYouTubeVideoId(url) ?: return null
        return "https://img.youtube.com/vi/$id/hqdefault.jpg"
    }

    fun extractVimeoId(url: String): String? {
        val regex = Regex("""vimeo\.com/(?:channels/[^/]+/|groups/[^/]+/videos/|video/)?(\d{5,})(?:[?#/]|$)""", RegexOption.IGNORE_CASE)
        return regex.find(url)?.groupValues?.getOrNull(1)
    }

    fun isDirectMediaUrl(url: String): Boolean {
        val lower = url.lowercase(Locale.ROOT).substringBefore('?').substringBefore('#')
        if (lower.startsWith("rtsp://") || lower.startsWith("rtmp://")) return true
        val directExtensions = listOf(
            ".mp4", ".m4v", ".mkv", ".webm", ".mov", ".avi", ".flv", ".wmv", ".3gp", ".ts",
            ".m3u8", ".mpd", ".mp3", ".aac", ".flac", ".wav", ".m4a", ".ogg", ".opus", ".amr"
        )
        return directExtensions.any { lower.endsWith(it) }
    }

    fun isDirectAudioUrl(url: String): Boolean {
        val lower = url.lowercase(Locale.ROOT).substringBefore('?').substringBefore('#')
        val audioExtensions = listOf(".mp3", ".aac", ".flac", ".wav", ".m4a", ".ogg", ".opus", ".amr", ".wma")
        return audioExtensions.any { lower.endsWith(it) }
    }

    fun isWebEmbedLink(url: String): Boolean {
        val lower = url.trim().lowercase(Locale.ROOT)
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) return false
        if (isDirectMediaUrl(lower)) return false
        return extractYouTubeVideoId(lower) != null ||
            extractVimeoId(lower) != null ||
            lower.contains("youtube.com") ||
            lower.contains("youtu.be") ||
            lower.contains("soundcloud.com") ||
            lower.contains("open.spotify.com") ||
            lower.contains("dailymotion.com") ||
            lower.contains("instagram.com") ||
            lower.contains("facebook.com") ||
            lower.contains("fb.watch") ||
            lower.contains("twitch.tv")
    }

    fun getEmbedWebUrl(rawUrl: String): String {
        val url = normalizeUrl(rawUrl)
        val ytId = extractYouTubeVideoId(url)
        if (ytId != null) {
            return "https://www.youtube-nocookie.com/embed/$ytId?autoplay=1&playsinline=1&controls=0&rel=0&modestbranding=1&iv_load_policy=3&fs=0"
        }
        val vimeoId = extractVimeoId(url)
        if (vimeoId != null) {
            return "https://player.vimeo.com/video/$vimeoId?autoplay=1"
        }
        val lower = url.lowercase(Locale.ROOT)
        if (lower.contains("soundcloud.com")) {
            return "https://w.soundcloud.com/player/?url=${Uri.encode(url)}&auto_play=true&visual=true"
        }
        if (lower.contains("open.spotify.com/") && !lower.contains("/embed/")) {
            return url.replace("open.spotify.com/", "open.spotify.com/embed/")
        }
        return url
    }

    fun inferMediaType(rawUrl: String): String {
        val lower = rawUrl.trim().lowercase(Locale.ROOT)
        return when {
            isYouTubeShortsUrl(lower) -> "SHORT"
            lower.contains("music.youtube.com") ||
                lower.contains("soundcloud.com") ||
                lower.contains("open.spotify.com") ||
                isDirectAudioUrl(lower) -> "SONG"
            else -> "VIDEO"
        }
    }

    fun inferSourceName(rawUrl: String): String {
        val lower = rawUrl.trim().lowercase(Locale.ROOT)
        return when {
            lower.contains("music.youtube.com") -> "YouTube Music"
            isYouTubeShortsUrl(lower) -> "YouTube Shorts"
            isYouTubeUrl(lower) -> "YouTube"
            lower.contains("vimeo.com") -> "Vimeo"
            lower.contains("soundcloud.com") -> "SoundCloud"
            lower.contains("open.spotify.com") -> "Spotify"
            lower.contains("instagram.com") -> "Instagram"
            lower.contains("facebook.com") || lower.contains("fb.watch") -> "Facebook"
            lower.contains("twitch.tv") -> "Twitch"
            else -> runCatching {
                Uri.parse(normalizeUrl(rawUrl)).host?.removePrefix("www.") ?: "Web Link"
            }.getOrDefault("Web Link")
        }
    }

    fun inferDefaultTitle(rawUrl: String, mediaType: String): String {
        val url = normalizeUrl(rawUrl)
        val ytId = extractYouTubeVideoId(url)
        if (ytId != null) {
            return when (mediaType) {
                "SHORT" -> "YouTube Short ($ytId)"
                "SONG" -> "YouTube Audio ($ytId)"
                else -> "YouTube Video ($ytId)"
            }
        }
        val lastSegment = Uri.parse(url).lastPathSegment
            ?.substringBeforeLast('.')
            ?.replace('-', ' ')
            ?.replace('_', ' ')
            ?.trim()
        if (!lastSegment.isNullOrBlank() && lastSegment.length > 2) {
            return lastSegment.take(60)
        }
        return "${inferSourceName(url)} ${mediaType.lowercase().replaceFirstChar { it.uppercase() }}"
    }

    suspend fun extractPlayableStream(
        rawUrl: String,
        preferAudioOnly: Boolean = false
    ): ExtractedMediaStream? = withContext(Dispatchers.IO) {
        val normalized = normalizeUrl(rawUrl)
        if (normalized.isBlank()) return@withContext null

        if (isDirectMediaUrl(normalized)) {
            return@withContext ExtractedMediaStream(
                streamUrl = normalized,
                audioStreamUrl = if (isDirectAudioUrl(normalized)) normalized else null
            )
        }

        val ytId = extractYouTubeVideoId(normalized)
        if (ytId != null) {
            extractYouTubeStream(ytId, preferAudioOnly)?.let { return@withContext it }
            extractViaPiped(ytId, preferAudioOnly)?.let { return@withContext it }
            extractViaInvidious(ytId, preferAudioOnly)?.let { return@withContext it }
            return@withContext null
        }

        val vimeoId = extractVimeoId(normalized)
        if (vimeoId != null) {
            extractVimeoStream(vimeoId)?.let { return@withContext it }
        }

        extractFromHtmlPage(normalized)
    }

    private fun extractYouTubeStream(videoId: String, preferAudioOnly: Boolean): ExtractedMediaStream? {
        data class InnerTubeProfile(
            val clientName: String,
            val clientVersion: String,
            val androidSdkVersion: Int,
            val userAgent: String,
            val deviceMake: String? = null,
            val deviceModel: String? = null
        )

        val profiles = listOf(
            InnerTubeProfile(
                clientName = "ANDROID_VR",
                clientVersion = "1.60.19",
                androidSdkVersion = 32,
                userAgent = "com.google.android.apps.youtube.vr.oculus/1.60.19 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip",
                deviceMake = "Oculus",
                deviceModel = "Quest 3"
            ),
            InnerTubeProfile(
                clientName = "ANDROID",
                clientVersion = "19.29.37",
                androidSdkVersion = 30,
                userAgent = "com.google.android.youtube/19.29.37 (Linux; U; Android 11) gzip"
            )
        )

        for (profile in profiles) {
            var conn: HttpURLConnection? = null
            try {
                val clientJson = JSONObject().apply {
                    put("clientName", profile.clientName)
                    put("clientVersion", profile.clientVersion)
                    put("androidSdkVersion", profile.androidSdkVersion)
                    put("hl", "en")
                    put("gl", "US")
                    profile.deviceMake?.let { put("deviceMake", it) }
                    profile.deviceModel?.let { put("deviceModel", it) }
                }
                val payload = JSONObject().apply {
                    put("videoId", videoId)
                    put("contentCheckOk", true)
                    put("racyCheckOk", true)
                    put("context", JSONObject().apply {
                        put("client", clientJson)
                    })
                }
                conn = (URL("https://www.youtube.com/youtubei/v1/player?prettyPrint=false").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 6000
                    readTimeout = 7000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("User-Agent", profile.userAgent)
                }
                OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
                if (conn.responseCode !in 200..299) continue

                val root = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val streamingData = root.optJSONObject("streamingData") ?: continue
                val videoDetails = root.optJSONObject("videoDetails")
                val title = videoDetails?.optString("title")?.takeIf { it.isNotBlank() }
                val author = videoDetails?.optString("author")?.takeIf { it.isNotBlank() }
                val thumb = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

                val formats = streamingData.optJSONArray("formats") ?: JSONArray()
                val adaptive = streamingData.optJSONArray("adaptiveFormats") ?: JSONArray()

                var bestVideoUrl: String? = null
                var bestVideoMime: String? = null
                var bestHeight = -1
                for (i in 0 until formats.length()) {
                    val f = formats.optJSONObject(i) ?: continue
                    val url = f.optString("url").takeIf { it.isNotBlank() } ?: continue
                    val mime = f.optString("mimeType").substringBefore(';').trim()
                    val h = f.optInt("height", 0)
                    if (mime.contains("mp4", true) && h >= bestHeight) {
                        bestHeight = h
                        bestVideoUrl = url
                        bestVideoMime = mime
                    } else if (bestVideoUrl == null) {
                        bestVideoUrl = url
                        bestVideoMime = mime.takeIf { it.isNotBlank() }
                    }
                }

                var bestAudioUrl: String? = null
                var bestAudioMime: String? = null
                var bestBitrate = -1
                for (i in 0 until adaptive.length()) {
                    val f = adaptive.optJSONObject(i) ?: continue
                    val url = f.optString("url").takeIf { it.isNotBlank() } ?: continue
                    val mime = f.optString("mimeType").substringBefore(';').trim()
                    if (mime.startsWith("audio/", true)) {
                        val br = f.optInt("bitrate", 0)
                        if (mime.contains("mp4", true) && br >= bestBitrate) {
                            bestBitrate = br
                            bestAudioUrl = url
                            bestAudioMime = mime
                        } else if (bestAudioUrl == null) {
                            bestAudioUrl = url
                            bestAudioMime = mime
                        }
                    }
                }

                val hls = streamingData.optString("hlsManifestUrl").takeIf { it.isNotBlank() }
                val chosenUrl = if (preferAudioOnly) {
                    bestAudioUrl ?: bestVideoUrl ?: hls
                } else {
                    bestVideoUrl ?: hls ?: bestAudioUrl
                } ?: continue

                val chosenMime = when {
                    preferAudioOnly && bestAudioUrl != null -> bestAudioMime
                    chosenUrl == hls -> "application/x-mpegURL"
                    else -> bestVideoMime
                }

                return ExtractedMediaStream(
                    streamUrl = chosenUrl,
                    audioStreamUrl = bestAudioUrl ?: bestVideoUrl,
                    title = title,
                    author = author,
                    thumbnailUrl = thumb,
                    mimeType = chosenMime
                )
            } catch (_: Exception) {
                // Try next InnerTube profile
            } finally {
                conn?.disconnect()
            }
        }
        return null
    }

    private fun extractViaPiped(videoId: String, preferAudioOnly: Boolean): ExtractedMediaStream? {
        val hosts = listOf(
            "https://pipedapi.kavin.rocks",
            "https://pipedapi.adminforge.de",
            "https://pipedapi.in.projectsegfau.lt"
        )
        for (host in hosts) {
            var conn: HttpURLConnection? = null
            try {
                conn = (URL("$host/streams/$videoId").openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5500
                    setRequestProperty("Accept", "application/json")
                }
                if (conn.responseCode !in 200..299) continue
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val videoStreams = json.optJSONArray("videoStreams") ?: JSONArray()
                val audioStreams = json.optJSONArray("audioStreams") ?: JSONArray()

                var muxedUrl: String? = null
                var muxedMime: String? = null
                for (i in 0 until videoStreams.length()) {
                    val s = videoStreams.optJSONObject(i) ?: continue
                    val u = s.optString("url").takeIf { it.isNotBlank() } ?: continue
                    if (!s.optBoolean("videoOnly", true)) {
                        muxedUrl = u
                        muxedMime = s.optString("mimeType").substringBefore(';').trim().takeIf { it.isNotBlank() }
                        break
                    }
                }
                var audioUrl: String? = null
                var audioMime: String? = null
                for (i in 0 until audioStreams.length()) {
                    val s = audioStreams.optJSONObject(i) ?: continue
                    val u = s.optString("url").takeIf { it.isNotBlank() } ?: continue
                    audioUrl = u
                    audioMime = s.optString("mimeType").substringBefore(';').trim().takeIf { it.isNotBlank() }
                    if (audioMime?.contains("mp4", true) == true) break
                }
                val hls = json.optString("hls").takeIf { it.isNotBlank() }
                val primary = if (preferAudioOnly) {
                    audioUrl ?: muxedUrl ?: hls
                } else {
                    muxedUrl ?: hls ?: audioUrl
                } ?: continue
                return ExtractedMediaStream(
                    streamUrl = primary,
                    audioStreamUrl = audioUrl ?: muxedUrl,
                    title = json.optString("title").takeIf { it.isNotBlank() },
                    author = json.optString("uploader").takeIf { it.isNotBlank() },
                    thumbnailUrl = json.optString("thumbnailUrl").takeIf { it.isNotBlank() },
                    mimeType = if (primary == hls) "application/x-mpegURL" else if (preferAudioOnly) audioMime else muxedMime
                )
            } catch (_: Exception) {
                // try next mirror
            } finally {
                conn?.disconnect()
            }
        }
        return null
    }

    private fun extractViaInvidious(videoId: String, preferAudioOnly: Boolean): ExtractedMediaStream? {
        val hosts = listOf(
            "https://inv.nadeko.net",
            "https://invidious.nerdvpn.de"
        )
        for (host in hosts) {
            var conn: HttpURLConnection? = null
            try {
                conn = (URL("$host/api/v1/videos/$videoId").openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5500
                    setRequestProperty("Accept", "application/json")
                }
                if (conn.responseCode !in 200..299) continue
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val formatStreams = json.optJSONArray("formatStreams") ?: JSONArray()
                val adaptiveFormats = json.optJSONArray("adaptiveFormats") ?: JSONArray()

                var videoUrl: String? = null
                var videoMime: String? = null
                for (i in 0 until formatStreams.length()) {
                    val s = formatStreams.optJSONObject(i) ?: continue
                    val u = s.optString("url").takeIf { it.isNotBlank() } ?: continue
                    videoUrl = u
                    videoMime = s.optString("type").substringBefore(';').trim().takeIf { it.isNotBlank() }
                    if (videoMime?.contains("mp4", true) == true) break
                }

                var audioUrl: String? = null
                var audioMime: String? = null
                for (i in 0 until adaptiveFormats.length()) {
                    val s = adaptiveFormats.optJSONObject(i) ?: continue
                    val type = s.optString("type").substringBefore(';').trim()
                    if (type.startsWith("audio/", true)) {
                        val u = s.optString("url").takeIf { it.isNotBlank() } ?: continue
                        audioUrl = u
                        audioMime = type
                        if (type.contains("mp4", true)) break
                    }
                }

                val hls = json.optString("hlsUrl").takeIf { it.isNotBlank() }
                val primary = if (preferAudioOnly) {
                    audioUrl ?: videoUrl ?: hls
                } else {
                    videoUrl ?: hls ?: audioUrl
                } ?: continue

                return ExtractedMediaStream(
                    streamUrl = primary,
                    audioStreamUrl = audioUrl ?: videoUrl,
                    title = json.optString("title").takeIf { it.isNotBlank() },
                    author = json.optString("author").takeIf { it.isNotBlank() },
                    mimeType = if (primary == hls) "application/x-mpegURL" else if (preferAudioOnly) audioMime else videoMime
                )
            } catch (_: Exception) {
                // try next mirror
            } finally {
                conn?.disconnect()
            }
        }
        return null
    }

    private fun extractVimeoStream(vimeoId: String): ExtractedMediaStream? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL("https://player.vimeo.com/video/$vimeoId/config").openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5500
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            if (conn.responseCode !in 200..299) return null
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val files = json.optJSONObject("request")?.optJSONObject("files") ?: return null
            val progressive = files.optJSONArray("progressive")
            var bestUrl: String? = null
            var bestWidth = -1
            if (progressive != null) {
                for (i in 0 until progressive.length()) {
                    val item = progressive.optJSONObject(i) ?: continue
                    val w = item.optInt("width", 0)
                    val u = item.optString("url").takeIf { it.isNotBlank() } ?: continue
                    if (w >= bestWidth) {
                        bestWidth = w
                        bestUrl = u
                    }
                }
            }
            if (bestUrl == null) {
                val hlsObj = files.optJSONObject("hls")
                val cdns = hlsObj?.optJSONObject("cdns")
                val defaultCdn = hlsObj?.optString("default_cdn")
                if (!defaultCdn.isNullOrBlank()) {
                    bestUrl = cdns?.optJSONObject(defaultCdn)?.optString("url")?.takeIf { it.isNotBlank() }
                }
            }
            bestUrl?.let {
                ExtractedMediaStream(
                    streamUrl = it,
                    title = json.optJSONObject("video")?.optString("title")?.takeIf { t -> t.isNotBlank() },
                    author = json.optJSONObject("video")?.optJSONObject("owner")?.optString("name")?.takeIf { a -> a.isNotBlank() },
                    mimeType = if (it.contains(".m3u8", true)) "application/x-mpegURL" else "video/mp4"
                )
            }
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun extractFromHtmlPage(pageUrl: String): ExtractedMediaStream? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(pageUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5500
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36")
            }
            val contentType = conn.contentType.orEmpty().lowercase(Locale.ROOT)
            if (contentType.startsWith("video/") || contentType.startsWith("audio/") || contentType.contains("mpegurl")) {
                return ExtractedMediaStream(streamUrl = conn.url.toString(), mimeType = contentType.substringBefore(';'))
            }
            if (conn.responseCode !in 200..299) return null
            val html = conn.inputStream.bufferedReader().use { it.readText() }.take(200_000)

            val metaVideo = Regex("""<meta[^>]+(?:property|name)=["'](?:og:video:secure_url|og:video:url|og:video|twitter:player:stream)["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                .find(html)?.groupValues?.getOrNull(1)
            val metaAudio = Regex("""<meta[^>]+(?:property|name)=["'](?:og:audio:secure_url|og:audio)["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                .find(html)?.groupValues?.getOrNull(1)
            val directTag = Regex("""<(?:video|audio|source)[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                .find(html)?.groupValues?.getOrNull(1)

            val rawCandidate = metaVideo ?: metaAudio ?: directTag ?: return null
            val resolved = URL(conn.url, rawCandidate.replace("&amp;", "&")).toString()
            ExtractedMediaStream(
                streamUrl = resolved,
                audioStreamUrl = metaAudio?.let { URL(conn.url, it.replace("&amp;", "&")).toString() }
            )
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }
}
