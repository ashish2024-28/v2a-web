package com.example.ui.components

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import java.util.Locale

enum class V2SDeviceType {
    PHONE,
    TABLET,
    LAPTOP_DESKTOP,
    TV
}

data class V2SDeviceInfo(
    val modelName: String,
    val manufacturer: String,
    val rawModel: String,
    val deviceType: V2SDeviceType,
    val deviceTypeLabel: String,
    val androidVersion: String,
    val apiLevel: Int,
    val timeGreeting: String,
    val hardwareSummary: String
)

object V2SDeviceDetector {

    fun getDetailedDeviceName(): String {
        val rawManufacturer = Build.MANUFACTURER.orEmpty()
        val manufacturer = rawManufacturer.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
        val model = Build.MODEL.orEmpty()
        val brand = Build.BRAND.orEmpty().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }

        return when {
            model.isBlank() -> if (manufacturer.isNotBlank()) manufacturer else "Android Device"
            model.startsWith(manufacturer, ignoreCase = true) -> model
            model.startsWith(brand, ignoreCase = true) -> model
            manufacturer.isNotBlank() -> "$manufacturer $model"
            else -> model
        }
    }

    fun getTimeOfDayGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }

    fun getDeviceInfo(context: Context, screenWidthDp: Int, smallestWidthDp: Int): V2SDeviceInfo {
        val modelName = getDetailedDeviceName()
        val rawManufacturer = Build.MANUFACTURER.orEmpty()
        val rawModel = Build.MODEL.orEmpty()
        val isChromebook = context.packageManager.hasSystemFeature("org.chromium.arc.device_management")
        val isTv = context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        val isTablet = smallestWidthDp >= 600 || screenWidthDp >= 600

        val deviceType = when {
            isTv -> V2SDeviceType.TV
            isChromebook || screenWidthDp >= 840 -> V2SDeviceType.LAPTOP_DESKTOP
            isTablet -> V2SDeviceType.TABLET
            else -> V2SDeviceType.PHONE
        }

        val deviceTypeLabel = when (deviceType) {
            V2SDeviceType.PHONE -> "Smartphone"
            V2SDeviceType.TABLET -> "Tablet"
            V2SDeviceType.LAPTOP_DESKTOP -> if (isChromebook) "Chromebook / Laptop" else "Laptop / Desktop"
            V2SDeviceType.TV -> "Android TV / Display"
        }

        val androidVersion = "Android ${Build.VERSION.RELEASE}"
        val apiLevel = Build.VERSION.SDK_INT
        val timeGreeting = getTimeOfDayGreeting()
        val hardwareSummary = "${Build.HARDWARE ?: "ARM"} • ${Build.BOARD ?: "Snapdragon/Exynos/MediaTek"}"

        return V2SDeviceInfo(
            modelName = modelName,
            manufacturer = rawManufacturer,
            rawModel = rawModel,
            deviceType = deviceType,
            deviceTypeLabel = deviceTypeLabel,
            androidVersion = androidVersion,
            apiLevel = apiLevel,
            timeGreeting = timeGreeting,
            hardwareSummary = hardwareSummary
        )
    }
}

@Composable
fun rememberV2SDeviceInfo(): V2SDeviceInfo {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    return remember(config.screenWidthDp, config.smallestScreenWidthDp) {
        V2SDeviceDetector.getDeviceInfo(
            context = context,
            screenWidthDp = config.screenWidthDp,
            smallestWidthDp = config.smallestScreenWidthDp
        )
    }
}

@Composable
fun rememberV2SDeviceType(): V2SDeviceType {
    val info = rememberV2SDeviceInfo()
    return info.deviceType
}
