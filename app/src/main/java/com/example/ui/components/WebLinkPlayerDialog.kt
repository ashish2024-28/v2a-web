package com.example.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.VlcOrange
import com.example.util.MediaLinkUtils

data class WebLinkPlayback(
    val title: String,
    val url: String,
    val sourceName: String,
    val isShort: Boolean = false,
    val mediaId: Long? = null
)

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebLinkPlayerDialog(
    playback: WebLinkPlayback,
    onClose: () -> Unit,
    onEditLink: (() -> Unit)? = null,
    onDeleteLink: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val embedUrl = remember(playback.url) { MediaLinkUtils.getEmbedWebUrl(playback.url) }
    val ytId = remember(playback.url) { MediaLinkUtils.extractYouTubeVideoId(playback.url) }

    BackHandler(onBack = onClose)

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var reloadSeed by remember { mutableIntStateOf(0) }
    var isLoading by remember(playback.url, reloadSeed) { mutableStateOf(true) }
    var loadProgress by remember(playback.url, reloadSeed) { mutableIntStateOf(10) }
    var loadError by remember(playback.url, reloadSeed) { mutableStateOf<String?>(null) }
    var bypassNotFoundCheck by remember(playback.url) { mutableStateOf(false) }

    LaunchedEffect(playback.url, reloadSeed, bypassNotFoundCheck) {
        if (!bypassNotFoundCheck && (ytId != null || playback.url.contains("vimeo.com", ignoreCase = true))) {
            val meta = MediaLinkUtils.fetchOEmbedMetadata(playback.url)
            if (meta.status == "not_found") {
                isLoading = false
                loadError = "This video could not be found (404 / removed or wrong video ID). Fix the link or open it in browser."
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                stopLoading()
                loadUrl("about:blank")
                destroy()
            }
            webViewRef = null
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121216))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Player",
                            tint = Color.White
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = playback.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = playback.sourceName,
                            color = VlcOrange,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onEditLink != null) {
                        IconButton(onClick = onEditLink) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Link",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            loadError = null
                            isLoading = true
                            reloadSeed++
                            webViewRef?.reload()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reload",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (isLoading && loadError == null) {
                LinearProgressIndicator(
                    progress = { (loadProgress.coerceIn(5, 100)) / 100f },
                    color = VlcOrange,
                    trackColor = Color(0xFF1F2029),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (loadError == null) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewRef = this
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setBackgroundColor(android.graphics.Color.BLACK)
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        loadProgress = newProgress
                                        if (newProgress >= 95) {
                                            isLoading = false
                                        }
                                    }
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val targetUrl = request?.url?.toString().orEmpty()
                                        val scheme = request?.url?.scheme?.lowercase().orEmpty()
                                        if (scheme != "http" && scheme != "https" && scheme != "about") {
                                            return true
                                        }
                                        val lowerTarget = targetUrl.lowercase()
                                        if (lowerTarget.contains("intent://") || lowerTarget.contains("vnd.youtube")) {
                                            return true
                                        }
                                        return false
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                        view?.evaluateJavascript(
                                            """
                                            (function(){
                                              var css='.ytp-chrome-top,.ytp-chrome-bottom,.ytp-pause-overlay,.ytp-endscreen-content,.ytp-watermark,.ytp-show-cards-title,.ytp-scroll-min,.ytp-paid-content-overlay,.ytp-ce-element{display:none!important;opacity:0!important;visibility:hidden!important;pointer-events:none!important;}';
                                              var apply=function(){
                                                var s=document.getElementById('v2s-clean-yt');
                                                if(!s){s=document.createElement('style');s.id='v2s-clean-yt';(document.head||document.documentElement).appendChild(s);}
                                                s.textContent=css;
                                              };
                                              apply();
                                              new MutationObserver(apply).observe(document.documentElement,{childList:true,subtree:true});
                                            })();
                                            """.trimIndent(),
                                            null
                                        )
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        if (request?.isForMainFrame == true) {
                                            isLoading = false
                                            val desc = error?.description?.toString()?.takeIf { it.isNotBlank() }
                                                ?: "Network error while loading this link."
                                            loadError = "Unable to fetch media page ($desc)."
                                        }
                                    }

                                    override fun onReceivedHttpError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        errorResponse: WebResourceResponse?
                                    ) {
                                        if (request?.isForMainFrame == true && (errorResponse?.statusCode ?: 200) >= 400) {
                                            isLoading = false
                                            loadError = "Link returned HTTP ${errorResponse?.statusCode}. Check the URL or open externally."
                                        }
                                    }
                                }

                                if (ytId != null) {
                                    loadUrl(
                                        embedUrl,
                                        mapOf("Referer" to "https://www.youtube-nocookie.com")
                                    )
                                } else {
                                    loadUrl(embedUrl)
                                }
                            }
                        },
                        update = { view ->
                            webViewRef = view
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF15161E),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "Unable to Fetch Video",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = loadError.orEmpty(),
                                color = Color(0xFFB8BCC8),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = playback.url,
                                color = Color(0xFF818799),
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        bypassNotFoundCheck = true
                                        loadError = null
                                        isLoading = true
                                        reloadSeed++
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VlcOrange,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                                    Text(" Try Anyway", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                if (onEditLink != null) {
                                    OutlinedButton(onClick = onEditLink) {
                                        Icon(Icons.Filled.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Text(" Fix Link", color = Color.White, fontSize = 12.sp)
                                    }
                                }

                                if (onDeleteLink != null) {
                                    OutlinedButton(onClick = onDeleteLink) {
                                        Icon(Icons.Filled.Delete, null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(16.dp))
                                        Text(" Remove", color = Color(0xFFFF6B6B), fontSize = 12.sp)
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
