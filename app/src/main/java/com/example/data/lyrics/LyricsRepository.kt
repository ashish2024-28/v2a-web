package com.example.data.lyrics

import android.content.Context
import com.example.data.model.SongItem
import java.util.Locale

/**
 * Predefined/offline lyrics provider.
 *
 * IMPORTANT:
 * - Only lyrics bundled inside the application are read.
 * - No network lookup is performed.
 * - No lyrics are generated/transcribed from audio.
 * - User sidecar .lrc/.txt files are deliberately ignored.
 *
 * Add approved lyrics as:
 *   app/src/main/assets/lyrics/<normalized-song-key>.lrc
 *
 * The key is based on title + artist. Example:
 *   "my song - artist.lrc"
 */
data class LyricLine(val timeMs: Long?, val text: String)

data class LyricsDocument(
    val lines: List<LyricLine>,
    val source: Source
) {
    enum class Source { PREDEFINED_LRC, PREDEFINED_TEXT, NONE }
    val hasTiming: Boolean get() = lines.any { it.timeMs != null }
}

object LyricsRepository {
    private const val ASSET_DIR = "lyrics"
    private val timePattern = Regex("""\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?\]""")

    fun load(context: Context, song: SongItem): LyricsDocument {
        val keys = linkedSetOf(
            normalizeKey(song.title, song.artist),
            normalizeKey(song.title, ""),
            song.id.toString()
        )

        for (key in keys) {
            for (extension in listOf("lrc", "txt")) {
                val assetName = "$ASSET_DIR/$key.$extension"
                val text = runCatching {
                    context.assets.open(assetName).bufferedReader(Charsets.UTF_8).use { it.readText() }
                }.getOrNull() ?: continue

                val lines = if (extension == "lrc") parseLrc(text) else parsePlain(text)
                if (lines.isNotEmpty()) {
                    return LyricsDocument(
                        lines,
                        if (extension == "lrc") LyricsDocument.Source.PREDEFINED_LRC
                        else LyricsDocument.Source.PREDEFINED_TEXT
                    )
                }
            }
        }

        return LyricsDocument(emptyList(), LyricsDocument.Source.NONE)
    }

    private fun normalizeKey(title: String, artist: String): String {
        return "$title${if (artist.isNotBlank()) " - $artist" else ""}"
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
    }

    fun parseLrc(text: String): List<LyricLine> =
        text.lineSequence().flatMap { line ->
            val matches = timePattern.findAll(line).toList()
            val lyricText = line.replace(timePattern, "").trim()
            if (matches.isEmpty() || lyricText.isBlank()) {
                emptySequence()
            } else {
                matches.asSequence().map { match ->
                    val fraction = match.groupValues[3].ifBlank { "0" }.toLong()
                    val millis = when (match.groupValues[3].length) {
                        1 -> fraction * 100
                        2 -> fraction * 10
                        else -> fraction
                    }
                    val timeMs =
                        (match.groupValues[1].toLong() * 60L +
                            match.groupValues[2].toLong()) * 1000L + millis
                    LyricLine(timeMs, lyricText)
                }
            }
        }.sortedBy { it.timeMs ?: Long.MAX_VALUE }.toList()

    fun parsePlain(text: String): List<LyricLine> =
        text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { LyricLine(null, it) }
            .toList()

    fun activeIndex(lines: List<LyricLine>, positionMs: Long): Int {
        var active = -1
        lines.forEachIndexed { index, line ->
            if (line.timeMs != null && line.timeMs <= positionMs) {
                active = index
            }
        }
        return active
    }
}
