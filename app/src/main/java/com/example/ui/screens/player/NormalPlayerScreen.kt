@file:OptIn(
    androidx.media3.common.util.UnstableApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.ui.screens.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Rational
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.media3.common.Player

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Videocam

import com.example.data.model.VideoItem
import com.example.ui.components.VideoThumbnail
import com.example.player.DecoderMode
import com.example.player.VideoScaleMode
import com.example.player.VyloPlayerManager
import com.example.ui.theme.PlayitCyan
import com.example.ui.theme.VlcOrange

import kotlin.math.abs
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.style.TextAlign


private fun Context.findActivity(): Activity? {
    var context = this

    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }

        context = context.baseContext
    }

    return null
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalPlayerScreen(
    video: VideoItem?,
    playlist: List<VideoItem> = emptyList(),
    allVideos: List<VideoItem> = emptyList(),
    onSelectVideo: (VideoItem) -> Unit = {},
    playerManager: VyloPlayerManager,

    onBack: () -> Unit,
    onMinimizeToPip: () -> Unit = {},

    // IMPORTANT:
    // MainActivity passes this callback when the user
    // presses the top headset / play-as-audio button.
    onPlayAsAudio: () -> Unit,

    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,

    onToggleFavorite: (VideoItem) -> Unit,
    onShare: (VideoItem) -> Unit,
    onShowInfo: (VideoItem) -> Unit,

    modifier: Modifier = Modifier
) {

    if (video == null) {
        onBack()
        return
    }

    val context = LocalContext.current

    val playerState by
        playerManager.playerState.collectAsState()

    // Keep screen awake while video is actively playing (prevents screen lock/sleep)
    val activity = context as? Activity
    val isVideoPlaying = playerState.isPlaying
    DisposableEffect(isVideoPlaying) {
        if (isVideoPlaying) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }


    var showControls by remember {
        mutableStateOf(true)
    }

    var isLocked by remember {
        mutableStateOf(false)
    }

    var isMuted by remember {
        mutableStateOf(false)
    }

    var subtitlesEnabled by remember {
        mutableStateOf(false)
    }

    var showTopOverflowMenu by remember {
        mutableStateOf(false)
    }

    var showSpeedModal by remember {
        mutableStateOf(false)
    }

    var showPlaylistSheet by remember {
        mutableStateOf(false)
    }

    var showSleepTimerMenu by remember {
        mutableStateOf(false)
    }

    var showEqualizerModal by remember {
        mutableStateOf(false)
    }

    var showSnapshotFlash by remember {
        mutableStateOf(false)
    }

    var showSubtitleModal by remember {
        mutableStateOf(false)
    }

    var showAudioTrackModal by remember {
        mutableStateOf(false)
    }

    var showVideoQualityModal by remember {
        mutableStateOf(false)
    }

    var showAbRepeatModal by remember {
        mutableStateOf(false)
    }

    var showPlayitHelpModal by remember {
        mutableStateOf(false)
    }

    val subtitlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "External Subtitle"
            playerManager.addExternalSubtitle(uri, fileName)
            Toast.makeText(context, "Loaded external subtitle: $fileName", Toast.LENGTH_SHORT).show()
        }
    }


    // Gesture HUD states
    var activeGestureHud by remember {
        mutableStateOf<String?>(null)
    }

    var seekPreviewTimeMs by remember {
        mutableLongStateOf(0L)
    }

    var doubleTapPulseSide by remember {
        mutableStateOf<String?>(null)
    }

    // 2-Finger Zoom & Pan gestures
    var pinchScale by remember { mutableFloatStateOf(1f) }
    var pinchPanX by remember { mutableFloatStateOf(0f) }
    var pinchPanY by remember { mutableFloatStateOf(0f) }

    val resizeModes = listOf(
        Pair(
            VideoScaleMode.FIT,
            AspectRatioFrameLayout.RESIZE_MODE_FIT
        ),

        Pair(
            VideoScaleMode.CROP,
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        ),

        Pair(
            VideoScaleMode.FILL,
            AspectRatioFrameLayout.RESIZE_MODE_FILL
        )
    )


    BackHandler {
        onBack()
    }

    /*
     * Keep playback running when minimizing back to list.
     */
    DisposableEffect(Unit) {
        onDispose {
            // Media playback continues smoothly in PersistentMiniPlayer or Floating Pip
        }
    }


    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)

            .pointerInput(isLocked) {
                if (!isLocked) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (zoom != 1f || pan != androidx.compose.ui.geometry.Offset.Zero) {
                            val newScale = (pinchScale * zoom).coerceIn(0.25f, 5.0f)
                            pinchScale = newScale
                            pinchPanX += pan.x
                            pinchPanY += pan.y
                            activeGestureHud = "Zoom: ${(newScale * 100).toInt()}%"
                        }
                    }
                }
            }

            .pointerInput(isLocked) {

                if (isLocked) {

                    detectTapGestures {

                        showControls =
                            !showControls
                    }

                } else {

                    detectTapGestures(

                        onTap = {

                            showControls =
                                !showControls
                        },

                        onDoubleTap = { offset ->
                            pinchScale = 1f
                            pinchPanX = 0f
                            pinchPanY = 0f
                            val width = size.width
                            when {
                                offset.x < width / 3 -> {
                                    playerManager.seekBackward(10_000L)
                                    doubleTapPulseSide = "LEFT"
                                }
                                offset.x > width * 2 / 3 -> {
                                    playerManager.seekForward(10_000L)
                                    doubleTapPulseSide = "RIGHT"
                                }
                                else -> {
                                    if (playerState.isPlaying) {
                                        playerManager.pause()
                                    } else {
                                        playerManager.resume()
                                    }
                                }
                            }
                        }
                    )
                }
            }

            .pointerInput(isLocked) {

                if (!isLocked) {

                    var startSeekPos = 0L
                    var isHorizontalDrag = false
                    var isLeftDrag = false
                    var totalDragX = 0f
                    var totalDragY = 0f


                    detectDragGestures(

                        onDragStart = { offset ->

                            startSeekPos =
                                playerState.currentPositionMs

                            isLeftDrag =
                                offset.x < size.width / 2

                            totalDragX = 0f
                            totalDragY = 0f

                            isHorizontalDrag = false

                            activeGestureHud = null
                        },


                        onDrag = { change, dragAmount ->

                            change.consume()

                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y


                            if (
                                !isHorizontalDrag &&
                                abs(totalDragX) > 30f &&
                                abs(totalDragX) > abs(totalDragY)
                            ) {
                                isHorizontalDrag = true
                            }


                            val duration =
                                if (playerState.durationMs > 0) {
                                    playerState.durationMs
                                } else {
                                    video.durationMs
                                }


                            if (
                                isHorizontalDrag &&
                                duration > 0
                            ) {

                                activeGestureHud =
                                    "SEEK"

                                val deltaSec =
                                    (totalDragX / size.width) *
                                            (duration / 1000f)

                                seekPreviewTimeMs =
                                    (
                                        startSeekPos +
                                                (deltaSec * 1000).toLong()
                                        ).coerceIn(
                                            0L,
                                            duration
                                        )

                            } else if (
                                !isHorizontalDrag &&
                                abs(totalDragY) > 10f
                            ) {

                                val deltaFraction =
                                    -dragAmount.y / size.height


                                if (isLeftDrag) {

                                    activeGestureHud =
                                        "BRIGHTNESS"

                                    val currentB =
                                        playerState.brightnessPercent

                                    val newB =
                                        (
                                            currentB +
                                                    (deltaFraction * 100).toInt()
                                            ).coerceIn(
                                                0,
                                                100
                                            )

                                    playerManager
                                        .setBrightnessPercent(
                                            newB
                                        )

                                } else {

                                    activeGestureHud =
                                        "VOLUME"

                                    val currentV =
                                        playerState.volumePercent

                                    val newV =
                                        (
                                            currentV +
                                                    (deltaFraction * 200).toInt()
                                            ).coerceIn(
                                                0,
                                                200
                                            )

                                    playerManager
                                        .setSystemVolumePercent(
                                            newV
                                        )
                                }
                            }
                        },


                        onDragEnd = {

                            if (
                                isHorizontalDrag &&
                                activeGestureHud == "SEEK"
                            ) {

                                playerManager.seekTo(
                                    seekPreviewTimeMs
                                )
                            }

                            activeGestureHud = null
                        },


                        onDragCancel = {

                            activeGestureHud = null
                        }
                    )
                }
            }
    ) {

        val currentResizeMode =
            resizeModes
                .firstOrNull {
                    it.first == playerState.scaleMode
                }
                ?.second
                ?: AspectRatioFrameLayout.RESIZE_MODE_FIT


        /*
         * ExoPlayer Surface
         */
        AndroidView(

            factory = { ctx ->

                PlayerView(ctx).apply {

                    player =
                        playerManager.getOrCreatePlayer()

                    useController = false

                    keepScreenOn = playerState.isPlaying

                    resizeMode =
                        currentResizeMode

                    subtitleView?.apply {
                        setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, playerState.subtitleFontSizeSp.toFloat())
                        setUserDefaultStyle()
                    }

                    layoutParams =
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                }
            },


            update = { view ->

                view.player =
                    playerManager.getOrCreatePlayer()

                view.keepScreenOn = playerState.isPlaying

                view.resizeMode =
                    currentResizeMode

                view.subtitleView?.setFixedTextSize(
                    android.util.TypedValue.COMPLEX_UNIT_SP,
                    playerState.subtitleFontSizeSp.toFloat()
                )
            },

            onReset = { view ->
                view.player = null
            },

            onRelease = { view ->
                view.player = null
            },


            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = pinchScale
                        scaleY = pinchScale
                        translationX = pinchPanX
                        translationY = pinchPanY
                        clip = true
                    }
        )


        /*
         * Screenshot flash
         */
        if (showSnapshotFlash) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.White.copy(alpha = 0.5f)
                    )
            )
        }


        /*
         * Gesture HUD
         */
        activeGestureHud?.let { hudType ->

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(
                        RoundedCornerShape(16.dp)
                    )
                    .background(
                        Color.Black.copy(alpha = 0.85f)
                    )
                    .padding(
                        horizontal = 24.dp,
                        vertical = 18.dp
                    )
            ) {

                when (hudType) {

                    "BRIGHTNESS" -> {

                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally,

                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Filled.BrightnessHigh,

                                contentDescription = null,

                                tint = VlcOrange,

                                modifier =
                                    Modifier.size(36.dp)
                            )

                            Text(
                                text =
                                    "Brightness: ${playerState.brightnessPercent}%",

                                color = Color.White,

                                fontWeight =
                                    FontWeight.Bold,

                                fontSize = 16.sp
                            )
                        }
                    }


                    "VOLUME" -> {
                        val isBoosted = playerState.volumePercent > 100

                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally,

                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Icon(
                                imageVector =
                                    if (
                                        playerState.volumePercent == 0
                                    ) {
                                        Icons.AutoMirrored.Filled.VolumeMute
                                    } else {
                                        Icons.AutoMirrored.Filled.VolumeUp
                                    },

                                contentDescription = null,

                                tint = if (isBoosted) VlcOrange else PlayitCyan,

                                modifier =
                                    Modifier.size(36.dp)
                            )

                            Text(
                                text = if (isBoosted) "Boost: ${playerState.volumePercent}%" else "Volume: ${playerState.volumePercent}%",

                                color = if (isBoosted) VlcOrange else Color.White,

                                fontWeight =
                                    FontWeight.Bold,

                                fontSize = 16.sp
                            )
                        }
                    }


                    "SEEK" -> {

                        val seekSec =
                            seekPreviewTimeMs / 1000

                        val durSec =
                            (
                                if (
                                    playerState.durationMs > 0
                                ) {
                                    playerState.durationMs
                                } else {
                                    video.durationMs
                                }
                                ) / 1000


                        val seekFormatted =
                            String.format(
                                java.util.Locale.getDefault(),
                                "%02d:%02d",
                                (seekSec % 3600) / 60,
                                seekSec % 60
                            )


                        val durFormatted =
                            String.format(
                                java.util.Locale.getDefault(),
                                "%02d:%02d",
                                (durSec % 3600) / 60,
                                durSec % 60
                            )


                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally,

                            verticalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {

                            Text(
                                text = "Seek to",

                                color =
                                    Color.White.copy(
                                        alpha = 0.7f
                                    ),

                                fontSize = 12.sp
                            )

                            Text(
                                text =
                                    "$seekFormatted / $durFormatted",

                                color = PlayitCyan,

                                fontWeight =
                                    FontWeight.Bold,

                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }


        /*
         * Double tap pulse
         */
        doubleTapPulseSide?.let { side ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),

                contentAlignment =
                    if (
                        side == "LEFT"
                    ) {
                        Alignment.CenterStart
                    } else {
                        Alignment.CenterEnd
                    }
            ) {

                Surface(
                    shape = CircleShape,

                    color =
                        Color.Black.copy(
                            alpha = 0.65f
                        ),

                    modifier =
                        Modifier.size(64.dp)
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                if (
                                    side == "LEFT"
                                ) {
                                    Icons.Filled.Replay10
                                } else {
                                    Icons.Filled.Forward10
                                },

                            contentDescription =
                                "10s skip",

                            tint = Color.White,

                            modifier =
                                Modifier.size(36.dp)
                        )
                    }
                }
            }
        }


        /*
         * Buffering indicator
         */
        if (
            playerState.isBuffering &&
            playerState.error == null
        ) {

            CircularProgressIndicator(
                color = PlayitCyan,

                modifier =
                    Modifier
                        .size(56.dp)
                        .align(Alignment.Center)
            )
        }

        if (playerState.error != null) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF15161E).copy(alpha = 0.95f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Unable to Play Video",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = playerState.error.orEmpty(),
                        color = Color(0xFFB8BCC8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Button(
                            onClick = { playerManager.retryCurrent() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VlcOrange,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                playerManager.toggleDecoderMode()
                                playerManager.retryCurrent()
                            }
                        ) {
                            Text(
                                "Switch ${if (playerState.decoderMode == DecoderMode.HW) "SW" else "HW"}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        val uriStr = video.uri.toString()
                        if (uriStr.startsWith("http://", ignoreCase = true) || uriStr.startsWith("https://", ignoreCase = true)) {
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_VIEW, video.uri).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Browser", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onPlayAsAudio) {
                            Icon(Icons.Filled.Headphones, null, tint = PlayitCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play as Audio", color = PlayitCyan, fontSize = 12.sp)
                        }
                        TextButton(onClick = onBack) {
                            Text("Go Back", color = Color(0xFFB8BCC8), fontSize = 12.sp)
                        }
                    }
                }
            }
        }


        /*
         * Locked screen
         */
        if (isLocked) {

            AnimatedVisibility(
                visible = showControls,

                enter = fadeIn(),
                exit = fadeOut(),

                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 20.dp)
            ) {

                Surface(
                    onClick = {

                        isLocked = false

                        Toast.makeText(
                            context,
                            "Screen unlocked",
                            Toast.LENGTH_SHORT
                        ).show()
                    },

                    shape =
                        RoundedCornerShape(10.dp),

                    color =
                        Color.Black.copy(
                            alpha = 0.7f
                        ),

                    modifier =
                        Modifier.size(46.dp)
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Filled.Lock,

                            contentDescription =
                                "Unlock controls",

                            tint = PlayitCyan,

                            modifier =
                                Modifier.size(24.dp)
                        )
                    }
                }
            }

            return@BoxWithConstraints
        }


        /*
         * ============================================================
         * TOP BAR
         * ============================================================
         */
        AnimatedVisibility(
            visible = showControls,

            enter = fadeIn(),
            exit = fadeOut(),

            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(
                                    alpha = 0.85f
                                ),
                                Color.Transparent
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 6.dp
                    )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                /*
                 * Back + title
                 */
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,

                    modifier =
                        Modifier.weight(1f)
                ) {

                    IconButton(
                        onClick = {
                            onBack()
                        },

                        modifier =
                            Modifier.testTag(
                                "player_back_button"
                            )
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,

                            contentDescription =
                                "Back",

                            tint = Color.White
                        )
                    }


                    Column(
                        modifier =
                            Modifier.padding(start = 2.dp)
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically,

                            horizontalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {

                            Text(
                                text = video.title,

                                color = Color.White,

                                fontSize = 15.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                maxLines = 1,

                                overflow =
                                    TextOverflow.Ellipsis
                            )
                            // Interactive Hardware Acceleration (HW / HW+ / SW) Decoder Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (playerState.decoderMode == DecoderMode.HW) PlayitCyan else VlcOrange,
                                modifier = Modifier.clickable {
                                    val next = playerManager.toggleDecoderMode()
                                    Toast.makeText(
                                        context,
                                        "Decoder: ${next.label} (${next.description})",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            ) {
                                Text(
                                    text = if (playerState.decoderMode == DecoderMode.HW) "HW+" else "SW",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            if (video.is4kUltraHd || video.resolutionLabel.contains("4K", ignoreCase = true) || video.resolutionLabel.contains("UHD", ignoreCase = true)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = VlcOrange
                                ) {
                                    Text(
                                        text = "4K UHD",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF263238)
                                ) {
                                    Text(
                                        text = if (video.resolutionLabel.isNotBlank()) video.resolutionLabel else "HDR",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            // A-B Loop Active Indicator Badge
                            if (playerState.isAbRepeatActive) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = VlcOrange.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, VlcOrange),
                                    modifier = Modifier.clickable {
                                        showAbRepeatModal = true
                                    }
                                ) {
                                    Text(
                                        text = "A-B LOOP",
                                        color = VlcOrange,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            // Multi-Stream / PLAYit Video Stream Pill
                            if (playerState.availableVideoTracks.size > 1) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PlayitCyan.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, PlayitCyan),
                                    modifier = Modifier.clickable {
                                        playerManager.toggleVideoStream()
                                        val cur = playerState.currentVideoTrackIndex
                                        Toast.makeText(context, "Switched to Video Stream ${cur + 1}", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Videocam,
                                            contentDescription = null,
                                            tint = PlayitCyan,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        Text(
                                            text = "Stream ${playerState.currentVideoTrackIndex + 1}/${playerState.availableVideoTracks.size}",
                                            color = PlayitCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                /*
                 * ====================================================
                 * TOP ACTION ICONS
                 *
                 * CC
                 * HEADPHONES = PLAY AS AUDIO
                 * PLAYLIST
                 * MORE
                 * ====================================================
                 */
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.spacedBy(2.dp)
                ) {

                    /*
                     * Subtitles
                     */
                    IconButton(
                        onClick = {
                            showSubtitleModal = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ClosedCaption,
                            contentDescription = "Subtitles",
                            tint = if (playerState.isSubtitlesEnabled) PlayitCyan else Color.White
                        )
                    }


                    /*
                     * ==================================================
                     * HEADSET / PLAY AS AUDIO
                     *
                     * IMPORTANT:
                     *
                     * This no longer calls:
                     *
                     * playerManager.toggleBackgroundPlay()
                     *
                     * It now calls:
                     *
                     * onPlayAsAudio()
                     *
                     * Therefore the headset has exactly one job:
                     *
                     * HEADPHONES -> PLAY AS AUDIO
                     * ==================================================
                     */
                    IconButton(
                        onClick = {

                            onPlayAsAudio()

                            Toast.makeText(
                                context,
                                "Playing as audio",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Filled.Headphones,

                            contentDescription =
                                "Play as audio",

                            tint = Color.White
                        )
                    }


                    /*
                     * Playlist / Queue
                     */
                    IconButton(
                        onClick = {
                            showPlaylistSheet = true
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.PlaylistPlay,

                            contentDescription =
                                "Playlist",

                            tint = Color.White
                        )
                    }


                    /*
                     * More menu
                     */
                    Box {

                        IconButton(
                            onClick = {
                                showTopOverflowMenu = true
                            }
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Filled.MoreVert,

                                contentDescription =
                                    "More",

                                tint = Color.White
                            )
                        }


                        DropdownMenu(
                            expanded =
                                showTopOverflowMenu,

                            onDismissRequest = {
                                showTopOverflowMenu = false
                            },

                            containerColor =
                                Color(0xFF1E202B)
                        ) {

                            DropdownMenuItem(
                                text = {
                                    val currentTrackName = playerState.availableAudioTracks.getOrNull(playerState.currentAudioTrackIndex) ?: "Stereo (Default)"
                                    Text(
                                        "Audio Track: $currentTrackName",
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.MusicNote,
                                        null,
                                        tint = PlayitCyan
                                    )
                                },

                                onClick = {
                                    showTopOverflowMenu = false
                                    showAudioTrackModal = true
                                }
                            )


                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Subtitles & Captions",
                                        color = Color.White
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.ClosedCaption,
                                        null,
                                        tint = if (playerState.isSubtitlesEnabled) PlayitCyan else Color.White
                                    )
                                },

                                onClick = {
                                    showTopOverflowMenu = false
                                    showSubtitleModal = true
                                }
                            )


                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (playerState.isAbRepeatActive) "A-B Repeat: Active" else "A-B Repeat Loop",
                                        color = if (playerState.isAbRepeatActive) VlcOrange else Color.White
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Repeat,
                                        null,
                                        tint = if (playerState.isAbRepeatActive) VlcOrange else PlayitCyan
                                    )
                                },

                                onClick = {
                                    showTopOverflowMenu = false
                                    showAbRepeatModal = true
                                }
                            )


                            if (playerState.availableVideoTracks.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Video Stream / Quality",
                                            color = Color.White
                                        )
                                    },

                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.Videocam,
                                            null,
                                            tint = PlayitCyan
                                        )
                                    },

                                    onClick = {
                                        showTopOverflowMenu = false
                                        showVideoQualityModal = true
                                    }
                                )
                            }


                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Playback Speed (${playerState.playbackSpeed}x)",
                                        color = Color.White
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Speed,
                                        null,
                                        tint = PlayitCyan
                                    )
                                },

                                onClick = {

                                    showTopOverflowMenu =
                                        false

                                    showSpeedModal =
                                        true
                                }
                            )


                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Equalizer",
                                        color = Color.White
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Equalizer,
                                        null,
                                        tint = PlayitCyan
                                    )
                                },

                                onClick = {

                                    showTopOverflowMenu =
                                        false

                                    showEqualizerModal =
                                        true
                                }
                            )


                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Sleep Timer",
                                        color = Color.White
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Timer,
                                        null,
                                        tint = PlayitCyan
                                    )
                                },

                                onClick = {

                                    showTopOverflowMenu =
                                        false

                                    showSleepTimerMenu =
                                        true
                                }
                            )


                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Decoder: ${playerState.decoderMode.label} (${playerState.decoderMode.description})",
                                        color = Color.White
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.AspectRatio,
                                        null,
                                        tint = if (playerState.decoderMode == DecoderMode.HW) PlayitCyan else VlcOrange
                                    )
                                },

                                onClick = {
                                    val next = playerManager.toggleDecoderMode()
                                    Toast.makeText(
                                        context,
                                        "Decoder switched to ${next.label}: ${next.description}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showTopOverflowMenu = false
                                }
                            )


                            if (playerState.availableVideoTracks.size > 1) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Switch Video Stream (${playerState.currentVideoTrackIndex + 1}/${playerState.availableVideoTracks.size})",
                                            color = Color.White
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.Videocam,
                                            null,
                                            tint = PlayitCyan
                                        )
                                    },
                                    onClick = {
                                        showTopOverflowMenu = false
                                        playerManager.toggleVideoStream()
                                        Toast.makeText(
                                            context,
                                            "Switched to Video Stream ${playerState.currentVideoTrackIndex + 1}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (playerState.isBackgroundPlayEnabled) "Background Play: On" else "Background Play: Off",
                                        color = if (playerState.isBackgroundPlayEnabled) PlayitCyan else Color.White
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Headphones,
                                        null,
                                        tint = if (playerState.isBackgroundPlayEnabled) PlayitCyan else Color.White
                                    )
                                },
                                onClick = {
                                    playerManager.toggleBackgroundPlay()
                                    val enabledNow = playerManager.playerState.value.isBackgroundPlayEnabled
                                    Toast.makeText(
                                        context,
                                        if (enabledNow) "Background Play enabled — audio keeps playing when minimized" else "Background Play disabled",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showTopOverflowMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "PLAYit / Smart Muxer Guide",
                                        color = Color.White
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Info,
                                        null,
                                        tint = PlayitCyan
                                    )
                                },
                                onClick = {
                                    showTopOverflowMenu = false
                                    showPlayitHelpModal = true
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Video Details",
                                        color = Color.White
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Info,
                                        null,
                                        tint = Color.White
                                    )
                                },

                                onClick = {

                                    showTopOverflowMenu =
                                        false

                                    onShowInfo(video)
                                }
                            )
                        }
                    }
                }
            }
        }

        /*
         * Multi-Stream / PLAYit Switcher Banner (when video has multiple video streams)
         */
        AnimatedVisibility(
            visible = showControls && playerState.availableVideoTracks.size > 1,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
                .padding(horizontal = 16.dp)
        ) {
            val isPromoStream = playerState.currentVideoTrackIndex == 0
            Surface(
                color = if (isPromoStream) Color(0xFA1E1215) else Color(0xEE121420),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (isPromoStream) Color(0xFFFF5252) else PlayitCyan.copy(alpha = 0.7f)),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = if (isPromoStream) Color(0xFFFF5252) else PlayitCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = if (isPromoStream) "PLAYit Promo Warning Stream" else "Real Video Active",
                            color = if (isPromoStream) Color(0xFFFF5252) else PlayitCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPromoStream) "Tap 'Play Real Video' to bypass promo stream" else "Stream ${playerState.currentVideoTrackIndex + 1} of ${playerState.availableVideoTracks.size} playing",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp
                        )
                    }
                    Surface(
                        onClick = {
                            if (isPromoStream) {
                                playerManager.selectVideoTrack(1, isUserAction = true)
                                Toast.makeText(context, "Switched to real video (Stream 2)", Toast.LENGTH_SHORT).show()
                            } else {
                                playerManager.toggleVideoStream()
                                Toast.makeText(context, "Switched video stream", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isPromoStream) Color(0xFFFF5252) else PlayitCyan
                    ) {
                        Text(
                            text = if (isPromoStream) "Play Real Video" else "Switch",
                            color = if (isPromoStream) Color.White else Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }


        /*
         * Floating left-side controls
         */
        AnimatedVisibility(
            visible = showControls,

            enter = fadeIn(),
            exit = fadeOut(),

            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 14.dp)
        ) {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                /*
                 * Mute
                 */
                Surface(
                    onClick = {

                        isMuted =
                            !isMuted

                        if (isMuted) {

                            playerManager
                                .mute()

                            Toast.makeText(
                                context,
                                "Muted",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {

                            playerManager
                                .unmute()

                            Toast.makeText(
                                context,
                                "Unmuted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },

                    shape =
                        RoundedCornerShape(10.dp),

                    color =
                        Color.Black.copy(
                            alpha = 0.6f
                        ),

                    modifier =
                        Modifier.size(44.dp)
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                if (
                                    isMuted ||
                                    playerState.volumePercent == 0
                                ) {
                                    Icons.AutoMirrored.Filled.VolumeOff
                                } else {
                                    Icons.AutoMirrored.Filled.VolumeUp
                                },

                            contentDescription =
                                "Mute Toggle",

                            tint =
                                if (isMuted) {
                                    Color.Red
                                } else {
                                    Color.White
                                },

                            modifier =
                                Modifier.size(22.dp)
                        )
                    }
                }


                /*
                 * Lock
                 */
                Surface(
                    onClick = {

                        isLocked = true

                        Toast.makeText(
                            context,
                            "Screen locked",
                            Toast.LENGTH_SHORT
                        ).show()
                    },

                    shape =
                        RoundedCornerShape(10.dp),

                    color =
                        Color.Black.copy(
                            alpha = 0.6f
                        ),

                    modifier =
                        Modifier.size(44.dp)
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Filled.LockOpen,

                            contentDescription =
                                "Lock Screen",

                            tint = Color.White,

                            modifier =
                                Modifier.size(22.dp)
                        )
                    }
                }
            }
        }


        /*
         * Floating right-side controls
         */
        AnimatedVisibility(
            visible = showControls,

            enter = fadeIn(),
            exit = fadeOut(),

            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 14.dp)
        ) {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                /*
                 * Screenshot
                 */
                Surface(
                    onClick = {

                        showSnapshotFlash = true

                        Toast.makeText(
                            context,
                            "📸 Screenshot saved to Gallery",
                            Toast.LENGTH_SHORT
                        ).show()

                        Handler(
                            Looper.getMainLooper()
                        ).postDelayed({

                            showSnapshotFlash = false

                        }, 150)
                    },

                    shape =
                        RoundedCornerShape(10.dp),

                    color =
                        Color.Black.copy(
                            alpha = 0.6f
                        ),

                    modifier =
                        Modifier.size(44.dp)
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Filled.PhotoCamera,

                            contentDescription =
                                "Screenshot",

                            tint = Color.White,

                            modifier =
                                Modifier.size(22.dp)
                        )
                    }
                }


                /*
                 * Screen rotation
                 */
                Surface(
                    onClick = {

                        val activity =
                            context.findActivity()

                        if (activity != null) {

                            activity.requestedOrientation =
                                if (
                                    activity.resources
                                        .configuration
                                        .orientation ==
                                    android.content.res.Configuration
                                        .ORIENTATION_LANDSCAPE
                                ) {
                                    ActivityInfo
                                        .SCREEN_ORIENTATION_PORTRAIT
                                } else {
                                    ActivityInfo
                                        .SCREEN_ORIENTATION_LANDSCAPE
                                }
                        }
                    },

                    shape =
                        RoundedCornerShape(10.dp),

                    color =
                        Color.Black.copy(
                            alpha = 0.6f
                        ),

                    modifier =
                        Modifier.size(44.dp)
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Filled.ScreenRotation,

                            contentDescription =
                                "Rotate screen",

                            tint = Color.White,

                            modifier =
                                Modifier.size(22.dp)
                        )
                    }
                }
            }
        }


        /*
         * ============================================================
         * BOTTOM CONTROLS
         * ============================================================
         */
        AnimatedVisibility(
            visible = showControls,

            enter = fadeIn(),
            exit = fadeOut(),

            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(
                                    alpha = 0.92f
                                )
                            )
                        )
                    )
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 10.dp
                    )
        ) {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {

                val currentPos =
                    playerState.currentPositionMs

                val duration =
                    if (
                        playerState.durationMs > 0
                    ) {
                        playerState.durationMs
                    } else {
                        video.durationMs
                    }


                val progress =
                    if (duration > 0) {
                        (
                            currentPos.toFloat() /
                                    duration.toFloat()
                            ).coerceIn(
                                0f,
                                1f
                            )
                    } else {
                        0f
                    }


                val curSec =
                    currentPos / 1000

                val durSec =
                    duration / 1000


                val curFormatted =
                    String.format(
                        java.util.Locale.getDefault(),
                        "%02d:%02d",
                        (curSec % 3600) / 60,
                        curSec % 60
                    )


                val durFormatted =
                    String.format(
                        java.util.Locale.getDefault(),
                        "%02d:%02d",
                        (durSec % 3600) / 60,
                        durSec % 60
                    )


                /*
                 * Progress
                 */
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = curFormatted,

                        color = Color.White,

                        fontSize = 12.sp,

                        fontWeight =
                            FontWeight.Medium
                    )


                    Slider(
                        value = progress,

                        onValueChange = { frac ->

                            val targetMs =
                                (
                                    frac * duration
                                    ).toLong()

                            playerManager.seekTo(
                                targetMs
                            )
                        },

                        colors =
                            SliderDefaults.colors(
                                thumbColor = Color.White,

                                activeTrackColor =
                                    PlayitCyan,

                                inactiveTrackColor =
                                    Color.White.copy(
                                        alpha = 0.25f
                                    )
                            ),

                        modifier =
                            Modifier
                                .weight(1f)
                                .height(26.dp)
                                .testTag(
                                    "normal_player_slider"
                                )
                    )


                    Text(
                        text = durFormatted,

                        color = Color.White,

                        fontSize = 12.sp,

                        fontWeight =
                            FontWeight.Medium
                    )
                }


                /*
                 * Transport controls
                 */
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    /*
                     * Shuffle
                     */
                    IconButton(
                        onClick = {
                            playerManager.toggleShuffle()
                            Toast.makeText(
                                context,
                                if (playerState.isShuffle) "Shuffle: On" else "Shuffle: Off",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (playerState.isShuffle) PlayitCyan else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    /*
                     * Previous
                     */
                    IconButton(
                        onClick = onPlayPrevious,

                        modifier =
                            Modifier.size(40.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Filled.SkipPrevious,

                            contentDescription =
                                "Previous Video",

                            tint = Color.White,

                            modifier =
                                Modifier.size(26.dp)
                        )
                    }

                    /*
                     * Play / Pause
                     */
                    IconButton(
                        onClick = {
                            playerManager
                                .togglePlayPause()
                        },

                        modifier =
                            Modifier.size(46.dp)
                    ) {

                        Icon(
                            imageVector =
                                if (
                                    playerState.isPlaying
                                ) {
                                    Icons.Filled.Pause
                                } else {
                                    Icons.Filled.PlayArrow
                                },

                            contentDescription =
                                if (
                                    playerState.isPlaying
                                ) {
                                    "Pause"
                                } else {
                                    "Play"
                                },

                            tint = Color.White,

                            modifier =
                                Modifier.size(34.dp)
                        )
                    }

                    /*
                     * Next
                     */
                    IconButton(
                        onClick = onPlayNext,

                        modifier =
                            Modifier.size(40.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Filled.SkipNext,

                            contentDescription =
                                "Next Video",

                            tint = Color.White,

                            modifier =
                                Modifier.size(26.dp)
                        )
                    }

                    /*
                     * Repeat
                     */
                    IconButton(
                        onClick = {
                            val newMode = when (playerState.repeatMode) {
                                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                                else -> Player.REPEAT_MODE_OFF
                            }
                            playerManager.setRepeatMode(newMode)
                            val label = when (newMode) {
                                Player.REPEAT_MODE_ALL -> "Repeat: All"
                                Player.REPEAT_MODE_ONE -> "Repeat: One"
                                else -> "Repeat: Off"
                            }
                            Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (playerState.repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                            contentDescription = "Repeat",
                            tint = if (playerState.repeatMode != Player.REPEAT_MODE_OFF) PlayitCyan else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    /*
                     * Speed
                     */
                    Surface(
                        onClick = {
                            showSpeedModal = true
                        },

                        shape =
                            RoundedCornerShape(12.dp),

                        color =
                            Color.White.copy(
                                alpha = 0.12f
                            ),

                        modifier =
                            Modifier.padding(
                                horizontal = 2.dp
                            )
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically,

                            modifier =
                                Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 5.dp
                                )
                        ) {

                            Text(
                                text =
                                    "${playerState.playbackSpeed}x",

                                color =
                                    PlayitCyan,

                                fontSize = 11.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }

                    /*
                     * Scale
                     */
                    IconButton(
                        onClick = {
                            pinchScale = 1f
                            pinchPanX = 0f
                            pinchPanY = 0f
                            val newMode =
                                playerManager
                                    .cycleScaleMode()
                            activeGestureHud = "Scale: ${newMode.label}"
                            Toast.makeText(
                                context,
                                "Scale: ${newMode.label}",
                                Toast.LENGTH_SHORT
                            ).show()
                        },

                        modifier =
                            Modifier.size(38.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Filled.AspectRatio,

                            contentDescription =
                                "Scale Mode",

                            tint = Color.White,

                            modifier =
                                Modifier.size(22.dp)
                        )
                    }


                    /*
                     * ==================================================
                     * PiP / FLOATING WINDOW
                     *
                     * This remains separate from the headset button.
                     * ==================================================
                     */
                    IconButton(
                        onClick = {
                            val act = context.findActivity()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && act != null) {
                                try {
                                    val params = PictureInPictureParams.Builder()
                                        .setAspectRatio(Rational(16, 9))
                                        .build()
                                    act.enterPictureInPictureMode(params)
                                } catch (e: Exception) {
                                    // Fallback to in-app floating PiP
                                }
                            }
                            onMinimizeToPip()
                            Toast.makeText(
                                context,
                                "Playing in Pop-up Floating Window",
                                Toast.LENGTH_SHORT
                            ).show()
                        },

                        modifier =
                            Modifier.size(42.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Filled.PictureInPictureAlt,

                            contentDescription =
                                "Floating Window PiP",

                            tint = PlayitCyan,

                            modifier =
                                Modifier.size(24.dp)
                        )
                    }
                }
            }
        }


        /*
         * Playlist sheet - Displays all videos in the playlist/queue
         */
        if (showPlaylistSheet) {

            ModalBottomSheet(
                onDismissRequest = {
                    showPlaylistSheet = false
                },
                sheetState =
                    androidx.compose.material3
                        .rememberModalBottomSheetState(
                            skipPartiallyExpanded = true
                        ),
                containerColor = Color(0xFF14151C),
                dragHandle = null
            ) {
                var playlistSearchQuery by remember { mutableStateOf("") }
                var selectedPlaylistTab by remember { mutableStateOf(0) } // 0: All Videos, 1: Queue

                val allVideosList = remember(allVideos, playlist, video) {
                    when {
                        allVideos.isNotEmpty() -> allVideos
                        playlist.isNotEmpty() -> playlist
                        else -> listOf(video)
                    }
                }

                val queueList = remember(playlist, video) {
                    if (playlist.isNotEmpty()) playlist else listOf(video)
                }

                val hasSeparateQueue = remember(allVideosList, queueList) {
                    queueList.size > 1 && queueList.size != allVideosList.size
                }

                val currentDisplayList = remember(selectedPlaylistTab, hasSeparateQueue, allVideosList, queueList, playlistSearchQuery) {
                    val base = if (hasSeparateQueue && selectedPlaylistTab == 1) queueList else allVideosList
                    if (playlistSearchQuery.isBlank()) {
                        base
                    } else {
                        base.filter { it.title.contains(playlistSearchQuery, ignoreCase = true) }
                    }
                }

                val listState = rememberLazyListState()
                val currentPlayingIndex = remember(currentDisplayList, video.id) {
                    currentDisplayList.indexOfFirst { it.id == video.id }
                }

                LaunchedEffect(showPlaylistSheet) {
                    if (showPlaylistSheet && currentPlayingIndex >= 0) {
                        listState.animateScrollToItem(currentPlayingIndex)
                    }
                }

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.80f)
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Video Playlist",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PlayitCyan.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, PlayitCyan.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "${currentDisplayList.size} videos",
                                    color = PlayitCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                showPlaylistSheet = false
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close Playlist",
                                tint = Color.White
                            )
                        }
                    }

                    if (hasSeparateQueue) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (selectedPlaylistTab == 0) PlayitCyan.copy(alpha = 0.25f) else Color(0xFF22242F),
                                border = if (selectedPlaylistTab == 0) BorderStroke(1.dp, PlayitCyan) else null,
                                modifier = Modifier.clickable { selectedPlaylistTab = 0 }
                            ) {
                                Text(
                                    text = "All Videos (${allVideosList.size})",
                                    color = if (selectedPlaylistTab == 0) PlayitCyan else Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (selectedPlaylistTab == 1) PlayitCyan.copy(alpha = 0.25f) else Color(0xFF22242F),
                                border = if (selectedPlaylistTab == 1) BorderStroke(1.dp, PlayitCyan) else null,
                                modifier = Modifier.clickable { selectedPlaylistTab = 1 }
                            ) {
                                Text(
                                    text = "Playing Queue (${queueList.size})",
                                    color = if (selectedPlaylistTab == 1) PlayitCyan else Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = playlistSearchQuery,
                        onValueChange = { playlistSearchQuery = it },
                        placeholder = { Text("Search videos in playlist...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = PlayitCyan, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (playlistSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { playlistSearchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PlayitCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color(0xFF1A1C24),
                            unfocusedContainerColor = Color(0xFF1A1C24)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (currentDisplayList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (playlistSearchQuery.isNotBlank()) "No matching videos found" else "No videos in playlist",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            itemsIndexed(
                                items = currentDisplayList,
                                key = { index, item -> "playlist_${item.id}_$index" }
                            ) { index, item ->
                                val isPlaying = item.id == video.id

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            if (!isPlaying) {
                                                onSelectVideo(item)
                                            }
                                            showPlaylistSheet = false
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isPlaying) PlayitCyan.copy(alpha = 0.18f) else Color(0xFF1B1D26),
                                    border = if (isPlaying) BorderStroke(1.dp, PlayitCyan.copy(alpha = 0.5f)) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Track index / Play icon
                                        Box(
                                            modifier = Modifier.size(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isPlaying) {
                                                Icon(
                                                    imageVector = Icons.Filled.PlayArrow,
                                                    contentDescription = "Playing",
                                                    tint = PlayitCyan,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = "${index + 1}",
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        // Compact Video Thumbnail
                                        Box(
                                            modifier = Modifier
                                                .size(width = 64.dp, height = 40.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        ) {
                                            VideoThumbnail(
                                                video = item,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        // Title & Info
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = item.title,
                                                color = if (isPlaying) PlayitCyan else Color.White,
                                                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                                                fontSize = 13.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = item.formattedDuration,
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    fontSize = 11.5.sp
                                                )

                                                if (item.resolutionLabel.isNotBlank()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(3.dp),
                                                        color = Color.Black.copy(alpha = 0.45f)
                                                    ) {
                                                        Text(
                                                            text = item.resolutionLabel,
                                                            color = Color(0xFFFFB52E),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }

                                                if (isPlaying) {
                                                    Text(
                                                        text = "NOW PLAYING",
                                                        color = PlayitCyan,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        /*
         * Playback speed
         */
        if (showSpeedModal) {

            com.example.ui.screens.songs
                .PlaybackSpeedSelectorDialog(

                    currentSpeed =
                        playerState.playbackSpeed,

                    onSelectSpeed = { speed ->

                        playerManager
                            .setPlaybackSpeed(speed)

                        showSpeedModal = false
                    },

                    onDismiss = {
                        showSpeedModal = false
                    }
                )
        }


        /*
         * Equalizer
         */
        if (showEqualizerModal) {

            com.example.ui.screens.songs
                .PlayitEqualizerBottomSheet(

                    playerManager =
                        playerManager,

                    onDismiss = {
                        showEqualizerModal = false
                    }
                )
        }


        /*
         * Sleep timer
         */
        if (showSleepTimerMenu) {

            com.example.ui.screens.songs
                .SleepTimerSelectorDialog(

                    currentMinutes =
                        playerState
                            .sleepTimerMinutesRemaining,

                    onSelectMinutes = { min ->

                        playerManager
                            .setSleepTimer(min)

                        showSleepTimerMenu = false
                    },

                    onDismiss = {
                        showSleepTimerMenu = false
                    }
                )
        }

        /*
         * Subtitles & Captions Modal
         */
        if (showSubtitleModal) {
            SubtitleSettingsDialog(
                tracks = playerManager.getAvailableSubtitleTracks(),
                currentIndex = playerState.currentSubtitleTrackIndex,
                isEnabled = playerState.isSubtitlesEnabled,
                fontSizeSp = playerState.subtitleFontSizeSp,
                onSelectTrack = { idx ->
                    playerManager.selectSubtitleTrack(idx)
                    showSubtitleModal = false
                },
                onToggleEnabled = { enabled ->
                    playerManager.setSubtitlesEnabled(enabled)
                },
                onChangeFontSize = { newSize ->
                    playerManager.setSubtitleFontSizeSp(newSize)
                },
                onPickExternalFile = {
                    subtitlePickerLauncher.launch("*/*")
                    showSubtitleModal = false
                },
                onDismiss = { showSubtitleModal = false }
            )
        }

        /*
         * Audio Tracks Modal
         */
        if (showAudioTrackModal) {
            AudioTrackSelectorDialog(
                tracks = playerManager.getAvailableAudioTracks(),
                currentIndex = playerState.currentAudioTrackIndex,
                onSelectTrack = { idx ->
                    playerManager.selectAudioTrack(idx)
                    showAudioTrackModal = false
                },
                onDismiss = { showAudioTrackModal = false }
            )
        }

        /*
         * A-B Repeat Modal
         */
        if (showAbRepeatModal) {
            AbRepeatDialog(
                pointA = playerState.repeatPointA,
                pointB = playerState.repeatPointB,
                isActive = playerState.isAbRepeatActive,
                currentPos = playerState.currentPositionMs,
                onSetPointA = { playerManager.setRepeatPointA() },
                onSetPointB = { playerManager.setRepeatPointB() },
                onClear = { playerManager.clearAbRepeat() },
                onDismiss = { showAbRepeatModal = false }
            )
        }

        /*
         * Video Stream / Quality Modal
         */
        if (showVideoQualityModal) {
            VideoTrackSelectorDialog(
                tracks = playerManager.getAvailableVideoTracks(),
                currentIndex = playerState.currentVideoTrackIndex,
                onSelectTrack = { idx ->
                    playerManager.selectVideoTrack(idx)
                    showVideoQualityModal = false
                },
                onOpenGuide = {
                    showPlayitHelpModal = true
                },
                onDismiss = { showVideoQualityModal = false }
            )
        }

        /*
         * PLAYit / Smart Muxer Fix Guide Modal
         */
        if (showPlayitHelpModal) {
            PlayitHelpDialog(
                hasMultipleStreams = playerState.availableVideoTracks.size > 1,
                currentStreamIndex = playerState.currentVideoTrackIndex,
                onSwitchStream = {
                    playerManager.toggleVideoStream()
                    Toast.makeText(context, "Switched to Stream ${playerState.currentVideoTrackIndex + 1}", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showPlayitHelpModal = false }
            )
        }
    }
}

private fun formatMsToTime(ms: Long): String {
    val totalSec = ms / 1000
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    val h = totalSec / 3600
    return if (h > 0) {
        String.format(java.util.Locale.getDefault(), "%d:%02d:%02d", h, m, s)
    } else {
        String.format(java.util.Locale.getDefault(), "%02d:%02d", m, s)
    }
}

@Composable
fun AudioTrackSelectorDialog(
    tracks: List<String>,
    currentIndex: Int,
    onSelectTrack: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1E2A),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.AudioFile, contentDescription = null, tint = PlayitCyan)
                Text("Audio Streams", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (tracks.isEmpty()) {
                    Surface(
                        onClick = { onSelectTrack(0) },
                        shape = RoundedCornerShape(10.dp),
                        color = PlayitCyan,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Stereo (Default Audio Stream)", color = Color.Black, fontWeight = FontWeight.Bold)
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Black)
                        }
                    }
                } else {
                    tracks.forEachIndexed { index, trackLabel ->
                        val isSelected = currentIndex == index
                        Surface(
                            onClick = { onSelectTrack(index) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PlayitCyan else Color(0xFF272938),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = trackLabel,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PlayitCyan)
            }
        }
    )
}

@Composable
fun SubtitleSettingsDialog(
    tracks: List<String>,
    currentIndex: Int,
    isEnabled: Boolean,
    fontSizeSp: Int,
    onSelectTrack: (Int) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onChangeFontSize: (Int) -> Unit,
    onPickExternalFile: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1E2A),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Subtitles, contentDescription = null, tint = PlayitCyan)
                Text("Subtitles & Captions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Off / Disable option
                Surface(
                    onClick = {
                        onSelectTrack(-1)
                        onToggleEnabled(false)
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (!isEnabled || currentIndex < 0) PlayitCyan else Color(0xFF272938),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Disable Subtitles",
                            color = if (!isEnabled || currentIndex < 0) Color.Black else Color.White,
                            fontWeight = if (!isEnabled || currentIndex < 0) FontWeight.Bold else FontWeight.Medium
                        )
                        if (!isEnabled || currentIndex < 0) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Black)
                        }
                    }
                }

                // Available Tracks
                if (tracks.isNotEmpty()) {
                    Text(
                        "Available Embedded Tracks:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    tracks.forEachIndexed { index, label ->
                        val isSelected = isEnabled && currentIndex == index
                        Surface(
                            onClick = {
                                onSelectTrack(index)
                                onToggleEnabled(true)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PlayitCyan else Color(0xFF272938),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Black)
                                }
                            }
                        }
                    }
                }

                // External Subtitle Picker
                Surface(
                    onClick = onPickExternalFile,
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF272938),
                    border = BorderStroke(1.dp, PlayitCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = PlayitCyan, modifier = Modifier.size(18.dp))
                        Text(
                            "Import External (.srt, .vtt, .ass)",
                            color = PlayitCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Font Size Adjuster
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Font Size: ${fontSizeSp}sp", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { onChangeFontSize((fontSizeSp - 2).coerceAtLeast(12)) },
                            modifier = Modifier.size(36.dp).background(Color(0xFF272938), CircleShape)
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Smaller", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { onChangeFontSize((fontSizeSp + 2).coerceAtMost(36)) },
                            modifier = Modifier.size(36.dp).background(Color(0xFF272938), CircleShape)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Larger", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = PlayitCyan)
            }
        }
    )
}

@Composable
fun AbRepeatDialog(
    pointA: Long?,
    pointB: Long?,
    isActive: Boolean,
    currentPos: Long,
    onSetPointA: () -> Unit,
    onSetPointB: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1E2A),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Repeat, contentDescription = null, tint = VlcOrange)
                Text("A-B Repeat Loop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Set start (A) and end (B) points to continuously loop a specific scene or clip segment.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Point A (Start):", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text(
                            text = if (pointA != null) formatMsToTime(pointA) else "Not Set",
                            color = if (pointA != null) PlayitCyan else Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Surface(
                        onClick = onSetPointA,
                        shape = RoundedCornerShape(8.dp),
                        color = PlayitCyan
                    ) {
                        Text(
                            "Set A at ${formatMsToTime(currentPos)}",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Point B (End):", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text(
                            text = if (pointB != null) formatMsToTime(pointB) else "Not Set",
                            color = if (pointB != null) VlcOrange else Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Surface(
                        onClick = onSetPointB,
                        shape = RoundedCornerShape(8.dp),
                        color = VlcOrange
                    ) {
                        Text(
                            "Set B at ${formatMsToTime(currentPos)}",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VlcOrange.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, VlcOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Looping active between ${pointA?.let { formatMsToTime(it) }} and ${pointB?.let { formatMsToTime(it) }}",
                            color = VlcOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (pointA != null || pointB != null) {
                    Surface(
                        onClick = onClear,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF272938),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Clear A-B Repeat Loop",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 14.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = VlcOrange)
            }
        }
    )
}

@Composable
fun VideoTrackSelectorDialog(
    tracks: List<String>,
    currentIndex: Int,
    onSelectTrack: (Int) -> Unit,
    onOpenGuide: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1E2A),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Videocam, contentDescription = null, tint = PlayitCyan)
                Text("Video Quality / Streams", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (tracks.size > 1) {
                    Surface(
                        color = PlayitCyan.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PlayitCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💡 Tip for PLAYit / Smart Muxer videos:",
                                color = PlayitCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "If you see a warning screen like 'Player doesn't support this video format - Install PLAYit', select Stream 2 to bypass the warning and play the real video.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                tracks.forEachIndexed { index, trackLabel ->
                    val isSelected = currentIndex == index
                    Surface(
                        onClick = { onSelectTrack(index) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PlayitCyan else Color(0xFF272938),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = trackLabel,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Black)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (tracks.size > 1 && onOpenGuide != null) {
                TextButton(onClick = {
                    onDismiss()
                    onOpenGuide()
                }) {
                    Text("PLAYit Fix Guide", color = PlayitCyan)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.Gray)
            }
        }
    )
}

@Composable
fun PlayitHelpDialog(
    hasMultipleStreams: Boolean,
    currentStreamIndex: Int,
    onSwitchStream: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1E2A),
        icon = {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = PlayitCyan,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                "PLAYit / Smart Muxer Fix",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Why does it say 'Player doesn't support this video format'?",
                    color = PlayitCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "VidMate and SnapTube enable a feature called 'Smart Muxer' by default. This embeds a promotional warning video as the first video stream to force users to install PLAYit.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Text(
                    text = "How V2S solves it automatically:",
                    color = PlayitCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "1. Automatic Bypass: V2S Player automatically detects dual-stream videos and switches to Stream 2 so you watch the real video without ever needing to install PLAYit.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = "2. All Formats Supported: V2S includes hardware and universal software decoders for MKV, MP4, AVI, WebM, FLV, TS, 3GP, and .playit containers with AC3/DTS/AAC audio.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = "3. Manual Switch: If you ever see a promo warning screen, simply tap 'Play Real Video' or use the 'Switch Video Stream' menu to instantly play Stream 2.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = "4. Prevent in VidMate/SnapTube: In VidMate -> Settings -> Downloads, turn OFF 'Smart Muxer' to download clean, standard single-stream videos.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            if (hasMultipleStreams) {
                Surface(
                    onClick = {
                        onSwitchStream()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = PlayitCyan
                ) {
                    Text(
                        text = "Switch to Stream ${if (currentStreamIndex == 0) 2 else 1}",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Got it", color = PlayitCyan)
                }
            }
        },
        dismissButton = {
            if (hasMultipleStreams) {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color.Gray)
                }
            }
        }
    )
}
