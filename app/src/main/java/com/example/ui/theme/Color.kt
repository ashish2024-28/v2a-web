package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// VLC Signature Brand Colors
val VlcOrange = Color(0xFFFFD000)
val VlcOrangeLight = Color(0xFFFFE66D)
val VlcOrangeDark = Color(0xFFC7A600)
val VlcAmber = Color(0xFFFFE000)

// YouTube Red & PLAYit Blue Accents
val YouTubeRed = Color(0xFFFF0000)
val PlayitCyan = Color(0xFF00B4D8)
val PlayitBlue = Color(0xFF0077B6)

// Dark Theme Surfaces (VLC Obsidian / Slate Dark)
val VlcDarkBackground = Color(0xFF0D1117)
val VlcDarkSurface = Color(0xFF161B22)
val VlcDarkSurfaceVariant = Color(0xFF21262D)
val VlcDarkOnBackground = Color(0xFFF0F6FC)
val VlcDarkOnSurface = Color(0xFFE6EDF3)
val VlcDarkOnSurfaceVariant = Color(0xFF8B949E)
val VlcDarkOutline = Color(0xFF30363D)

// Light Theme Surfaces (VLC Clean Modern Light)
val VlcLightBackground = Color(0xFFF6F8FA)
val VlcLightSurface = Color(0xFFFFFFFF)
val VlcLightSurfaceVariant = Color(0xFFEAEEF2)
val VlcLightOnBackground = Color(0xFF1F2328)
val VlcLightOnSurface = Color(0xFF24292F)
val VlcLightOnSurfaceVariant = Color(0xFF57606A)
val VlcLightOutline = Color(0xFFD0D7DE)

// Compatibility Aliases (only the ones actually referenced elsewhere are kept)
val VyloVioletPrimary = VlcOrange
val VyloVioletDark = VlcOrangeDark
val VyloCyanAccent = PlayitCyan
val VyloCoralAccent = YouTubeRed



/** User-selectable accent palettes. */
enum class AccentColor(val label: String, val primary: Color, val lightPrimary: Color, val darkPrimary: Color) {
    YELLOW("Yellow", Color(0xFFFFD000), Color(0xFFB08C00), Color(0xFFFFD000)),
    BLUE("Blue", Color(0xFF2196F3), Color(0xFF1565C0), Color(0xFF64B5F6)),
    PURPLE("Purple", Color(0xFF9C27B0), Color(0xFF7B1FA2), Color(0xFFCE93D8)),
    GREEN("Green", Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF81C784)),
    RED("Red", Color(0xFFD32F2F), Color(0xFFB71C1C), Color(0xFFEF9A9A)),
    CYAN("Cyan", Color(0xFF00A7C4), Color(0xFF007C91), Color(0xFF80DEEA)),
    ORANGE("Orange", Color(0xFFFF7A00), Color(0xFFE65100), Color(0xFFFFB74D)),
    PINK("Pink", Color(0xFFE91E63), Color(0xFFC2185B), Color(0xFFF48FB1)),
    TEAL("Teal", Color(0xFF009688), Color(0xFF00695C), Color(0xFF80CBC4)),
    INDIGO("Indigo", Color(0xFF3F51B5), Color(0xFF303F9F), Color(0xFF9FA8DA))
}
