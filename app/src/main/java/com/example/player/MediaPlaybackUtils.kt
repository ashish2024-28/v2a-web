package com.example.player

import androidx.media3.common.MimeTypes

/**
 * Resolves a Media3 MIME hint for local/content/network media.
 *
 * MediaStore content:// URIs commonly end in a numeric ID, so URI sniffing alone
 * cannot reliably identify the container. A declared MediaStore MIME type or
 * filesystem extension is therefore preferred when available.
 */
object MediaPlaybackUtils {
    fun resolveMimeType(
        declaredMimeType: String?,
        path: String?,
        uriString: String?,
        isAudio: Boolean
    ): String? {
        val declared = declaredMimeType?.trim()?.lowercase()
        val isUsefulDeclaredMime = !declared.isNullOrBlank() &&
            declared != "*/*" &&
            declared != "audio/*" &&
            declared != "video/*" &&
            declared != "application/octet-stream" &&
            declared != "binary/octet-stream"
        if (isUsefulDeclaredMime) {
            return declared
        }

        val source = path?.takeIf { it.isNotBlank() } ?: uriString.orEmpty()
        val extension = source.substringBefore('?').substringBefore('#')
            .substringAfterLast('.', "")
            .lowercase()

        return when (extension) {
            "playit" -> {
                // PLAYit downloads: standard MP4, MKV, or TS with .playit extension
                when {
                    source.contains(".mkv", ignoreCase = true) -> MimeTypes.APPLICATION_MATROSKA
                    source.contains(".ts", ignoreCase = true) -> MimeTypes.VIDEO_MP2T
                    source.contains(".webm", ignoreCase = true) -> MimeTypes.VIDEO_WEBM
                    source.contains(".avi", ignoreCase = true) -> "video/x-msvideo"
                    else -> MimeTypes.VIDEO_MP4
                }
            }
            "m3u8" -> MimeTypes.APPLICATION_M3U8
            "mpd" -> MimeTypes.APPLICATION_MPD
            "ism", "isml" -> MimeTypes.APPLICATION_SS
            "mp4", "m4v" -> MimeTypes.VIDEO_MP4
            "m4a", "m4b" -> MimeTypes.AUDIO_MP4
            "mkv" -> MimeTypes.APPLICATION_MATROSKA
            "webm" -> MimeTypes.VIDEO_WEBM
            // Media3 does not expose constants for every container MIME type.
            // Keep these explicit strings so compilation does not depend on optional constants.
            "mov", "qt" -> "video/quicktime"
            "avi", "divx", "xvid" -> "video/x-msvideo"
            "flv", "f4v" -> "video/x-flv"
            "asf" -> "video/x-ms-asf"
            "wmv" -> "video/x-ms-wmv"
            "mpeg", "mpg", "mpe", "m2v", "vob" -> "video/mpeg"
            "ts", "mts", "m2ts" -> MimeTypes.VIDEO_MP2T
            "3gp", "3gpp" -> "video/3gpp"
            "3g2" -> "video/3gpp2"
            "ogv" -> "video/ogg"
            "rm", "rmvb" -> "video/x-pn-realvideo"
            "mp3" -> MimeTypes.AUDIO_MPEG
            "aac" -> MimeTypes.AUDIO_AAC
            "adts" -> MimeTypes.AUDIO_AAC
            "flac" -> MimeTypes.AUDIO_FLAC
            "ogg", "oga" -> if (isAudio) MimeTypes.AUDIO_OGG else "video/ogg"
            "opus" -> MimeTypes.AUDIO_OPUS
            "wav" -> MimeTypes.AUDIO_WAV
            "amr" -> MimeTypes.AUDIO_AMR
            "awb" -> MimeTypes.AUDIO_AMR_WB
            else -> null
        }
    }
}
