package com.example.player

import android.content.Context
import android.content.SharedPreferences

enum class EarphoneAction(val title: String, val description: String) {
    PLAY_PAUSE("Play / Pause", "Toggle play and pause on the current media"),
    NEXT_TRACK("Next Track / Video", "Skip to the next song or video"),
    PREVIOUS_TRACK("Previous Track / Video", "Skip to the previous song or video"),
    SEEK_FORWARD_10S("Seek Forward (+10s)", "Fast-forward by 10 seconds"),
    SEEK_BACKWARD_10S("Seek Backward (-10s)", "Rewind by 10 seconds"),
    INCREASE_VOLUME("Increase Volume (+10%)", "Turn system volume up"),
    DECREASE_VOLUME("Decrease Volume (-10%)", "Turn system volume down"),
    MUTE_UNMUTE("Mute / Unmute", "Silence or restore audio"),
    TOGGLE_SPEED("Speed Toggle (1.0x / 1.5x / 2.0x)", "Cycle through playback speeds"),
    NONE("No Action", "Do nothing on this gesture")
}

data class EarphoneControlSettings(
    val isCustomControlsEnabled: Boolean = true,
    val singleClickAction: EarphoneAction = EarphoneAction.PLAY_PAUSE,
    val doubleClickAction: EarphoneAction = EarphoneAction.NEXT_TRACK,
    val tripleClickAction: EarphoneAction = EarphoneAction.PREVIOUS_TRACK,
    val doubleClickRightAction: EarphoneAction = EarphoneAction.NEXT_TRACK,
    val doubleClickLeftAction: EarphoneAction = EarphoneAction.PREVIOUS_TRACK,
    val pauseOnUnplugEnabled: Boolean = true,
    val resumeOnPlugEnabled: Boolean = false,
    val callHandlingEnabled: Boolean = true,
    val clickIntervalMs: Long = 400L
)

object EarphonePreferences {
    private const val PREFS_NAME = "v2s_earphone_preferences"
    private const val KEY_CUSTOM_ENABLED = "custom_controls_enabled"
    private const val KEY_SINGLE_CLICK = "single_click_action"
    private const val KEY_DOUBLE_CLICK = "double_click_action"
    private const val KEY_TRIPLE_CLICK = "triple_click_action"
    private const val KEY_DOUBLE_RIGHT = "double_right_action"
    private const val KEY_DOUBLE_LEFT = "double_left_action"
    private const val KEY_PAUSE_ON_UNPLUG = "pause_on_unplug"
    private const val KEY_RESUME_ON_PLUG = "resume_on_plug"
    private const val KEY_CALL_HANDLING = "call_handling_enabled"
    private const val KEY_CLICK_INTERVAL = "click_interval_ms"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun load(context: Context): EarphoneControlSettings {
        val prefs = getPrefs(context)
        return EarphoneControlSettings(
            isCustomControlsEnabled = prefs.getBoolean(KEY_CUSTOM_ENABLED, true),
            singleClickAction = parseAction(prefs.getString(KEY_SINGLE_CLICK, EarphoneAction.PLAY_PAUSE.name), EarphoneAction.PLAY_PAUSE),
            doubleClickAction = parseAction(prefs.getString(KEY_DOUBLE_CLICK, EarphoneAction.NEXT_TRACK.name), EarphoneAction.NEXT_TRACK),
            tripleClickAction = parseAction(prefs.getString(KEY_TRIPLE_CLICK, EarphoneAction.PREVIOUS_TRACK.name), EarphoneAction.PREVIOUS_TRACK),
            doubleClickRightAction = parseAction(prefs.getString(KEY_DOUBLE_RIGHT, EarphoneAction.NEXT_TRACK.name), EarphoneAction.NEXT_TRACK),
            doubleClickLeftAction = parseAction(prefs.getString(KEY_DOUBLE_LEFT, EarphoneAction.PREVIOUS_TRACK.name), EarphoneAction.PREVIOUS_TRACK),
            pauseOnUnplugEnabled = prefs.getBoolean(KEY_PAUSE_ON_UNPLUG, true),
            resumeOnPlugEnabled = prefs.getBoolean(KEY_RESUME_ON_PLUG, false),
            callHandlingEnabled = prefs.getBoolean(KEY_CALL_HANDLING, true),
            clickIntervalMs = prefs.getLong(KEY_CLICK_INTERVAL, 400L)
        )
    }

    fun save(context: Context, settings: EarphoneControlSettings) {
        getPrefs(context).edit()
            .putBoolean(KEY_CUSTOM_ENABLED, settings.isCustomControlsEnabled)
            .putString(KEY_SINGLE_CLICK, settings.singleClickAction.name)
            .putString(KEY_DOUBLE_CLICK, settings.doubleClickAction.name)
            .putString(KEY_TRIPLE_CLICK, settings.tripleClickAction.name)
            .putString(KEY_DOUBLE_RIGHT, settings.doubleClickRightAction.name)
            .putString(KEY_DOUBLE_LEFT, settings.doubleClickLeftAction.name)
            .putBoolean(KEY_PAUSE_ON_UNPLUG, settings.pauseOnUnplugEnabled)
            .putBoolean(KEY_RESUME_ON_PLUG, settings.resumeOnPlugEnabled)
            .putBoolean(KEY_CALL_HANDLING, settings.callHandlingEnabled)
            .putLong(KEY_CLICK_INTERVAL, settings.clickIntervalMs)
            .apply()
    }

    private fun parseAction(name: String?, fallback: EarphoneAction): EarphoneAction {
        if (name == null) return fallback
        return try {
            EarphoneAction.valueOf(name)
        } catch (_: Exception) {
            fallback
        }
    }
}
