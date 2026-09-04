package com.example

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.adblock.AdBlockEngine
import com.example.data.AppPreferences
import com.example.model.AppUpdateInfo
import com.example.ui.components.AppDestination
import com.example.ui.components.Floating3DotMenu
import com.example.ui.components.UpdatePromptModal
import com.example.ui.screens.FeedbackScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UpdatesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.updates.UpdateManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var customViewContainer: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        customViewContainer = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }

        setContent {
            MyApplicationTheme {
                MainScreen(
                    onShowCustomView = { view, callback ->
                        customView = view
                        customViewCallback = callback

                        view.layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        customViewContainer?.addView(view)
                        customViewContainer?.visibility = View.VISIBLE
                        customViewContainer?.bringToFront()

                        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).apply {
                            hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                            systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        }

                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    },
                    onHideCustomView = {
                        customViewContainer?.visibility = View.GONE
                        customViewContainer?.removeView(customView)
                        customView = null
                        customViewCallback?.onCustomViewHidden()

                        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).apply {
                            show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                        }

                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                )
            }
        }

        val rootView = findViewById<ViewGroup>(android.R.id.content)
        rootView.addView(customViewContainer)
    }
}

const val DEFAULT_HOME_URL = "https://moview-online.com/movies/#newmovies"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MainScreen(
    onShowCustomView: (View, WebChromeClient.CustomViewCallback) -> Unit,
    onHideCustomView: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val adBlockEngine = remember { AdBlockEngine(context) }
    val updateManager = remember { UpdateManager(context) }
    val prefs = remember { AppPreferences.getInstance(context) }

    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }
    var isVideoPlaying by remember { mutableStateOf(false) }
    var currentVideoUrl by remember { mutableStateOf("") }
    var latestDetectedMediaUrl by remember { mutableStateOf("") }
    var pageTitle by remember { mutableStateOf("123Movies") }
    var currentUrl by remember { mutableStateOf(DEFAULT_HOME_URL) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorDescription by remember { mutableStateOf("") }

    // Navigation & Modal States
    var currentScreen by remember { mutableStateOf(AppDestination.NONE) }
    var latestAvailableUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var showStartupUpdateModal by remember { mutableStateOf(false) }

    // Check for updates on startup
    LaunchedEffect(Unit) {
        val update = updateManager.checkForUpdates()
        latestAvailableUpdate = update
        if (update.isNewer) {
            val lastPrompted = prefs.getLastPromptedUpdate()
            if (lastPrompted != update.version) {
                showStartupUpdateModal = true
                prefs.setLastPromptedUpdate(update.version)
            }
        }
    }

    // Determine if current URL corresponds to a movie watch/stream page
    val isMovieUrl = remember(currentUrl) {
        val lower = currentUrl.lowercase()
        lower.contains("/watch/") || lower.contains("/movie/") ||
        lower.contains("/film/") || lower.contains("/episode/") ||
        lower.contains("/player") || lower.contains("video_id=") ||
        lower.contains("v=")
    }

    // Floating 3-Dot Menu hides when user enters movie or video is playing or is in full screen
    val isFloatingMenuVisible = !isFullScreen && !isVideoPlaying && !isMovieUrl && currentScreen == AppDestination.NONE

    // Back navigation handling
    BackHandler(enabled = isFullScreen || currentScreen != AppDestination.NONE || canGoBack) {
        when {
            currentScreen != AppDestination.NONE -> {
                currentScreen = AppDestination.NONE
            }
            isFullScreen -> {
                onHideCustomView()
            }
            canGoBack -> {
                webView?.goBack()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Primary Web Content Container
        Scaffold(
            floatingActionButton = {
                // Download Button (Visible only when media stream is detected & playing)
                AnimatedVisibility(
                    visible = isVideoPlaying && currentVideoUrl.isNotEmpty() && currentScreen == AppDestination.NONE,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            val targetUrl = if (currentVideoUrl.startsWith("blob:")) latestDetectedMediaUrl else currentVideoUrl
                            if (targetUrl.isNotEmpty() && targetUrl.startsWith("http")) {
                                try {
                                    val request = DownloadManager.Request(Uri.parse(targetUrl))
                                    request.setTitle("123Movies Media Download")
                                    request.setDescription("Downloading video...")
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    request.setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_MOVIES,
                                        "123Movies_Video_${System.currentTimeMillis()}.mp4"
                                    )

                                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                    downloadManager.enqueue(request)
                                    Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot download this stream directly.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "No downloadable media source found.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        icon = { Icon(Icons.Filled.Download, "Download") },
                        text = { Text("Download") },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(bottom = 70.dp) // Offset above 3-dot FAB
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AndroidView(
                    factory = { ctx ->
                        val wv = WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.databaseEnabled = true
                            settings.allowFileAccess = true
                            settings.allowContentAccess = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.setSupportZoom(true)
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            settings.javaScriptCanOpenWindowsAutomatically = false // Prevent ad window spawning
                            settings.setSupportMultipleWindows(true)
                            settings.cacheMode = WebSettings.LOAD_DEFAULT
                            settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

                            try {
                                CookieManager.getInstance().setAcceptCookie(true)
                                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                            } catch (e: Exception) {
                                Log.e("WebView", "Error setting cookies", e)
                            }

                            // Bridge for Video Playback & Event Detection
                            addJavascriptInterface(object : Any() {
                                @JavascriptInterface
                                fun onVideoEvent(event: String, url: String?) {
                                    coroutineScope.launch {
                                        if (event == "play") {
                                            isVideoPlaying = true
                                            if (!url.isNullOrEmpty()) {
                                                currentVideoUrl = url
                                            }
                                        } else if (event == "pause" || event == "ended") {
                                            isVideoPlaying = false
                                        }
                                    }
                                }
                            }, "AndroidBridge")

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    hasError = false
                                    url?.let { currentUrl = it }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    canGoBack = view?.canGoBack() == true
                                    canGoForward = view?.canGoForward() == true
                                    view?.title?.let { if (it.isNotBlank()) pageTitle = it }
                                    url?.let { currentUrl = it }

                                    // Inject Multi-Layer AdBlock CSS/JS Shield
                                    view?.evaluateJavascript(adBlockEngine.adShieldInjectionScript, null)

                                    // Video element detection script
                                    val videoListenerJs = """
                                        javascript:(function() {
                                            setInterval(function() {
                                                var videos = document.querySelectorAll('video');
                                                for (var i = 0; i < videos.length; i++) {
                                                    var v = videos[i];
                                                    if (!v.dataset.hasListener) {
                                                        v.dataset.hasListener = "true";
                                                        v.addEventListener('play', function(e) {
                                                            if (window.AndroidBridge) {
                                                                window.AndroidBridge.onVideoEvent('play', e.target.currentSrc || e.target.src);
                                                            }
                                                        });
                                                        v.addEventListener('pause', function(e) {
                                                            if (window.AndroidBridge) {
                                                                window.AndroidBridge.onVideoEvent('pause', '');
                                                            }
                                                        });
                                                        v.addEventListener('ended', function(e) {
                                                            if (window.AndroidBridge) {
                                                                window.AndroidBridge.onVideoEvent('ended', '');
                                                            }
                                                        });
                                                    }
                                                }
                                            }, 1000);
                                        })();
                                    """.trimIndent()
                                    view?.evaluateJavascript(videoListenerJs, null)
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        isLoading = false
                                        hasError = true
                                        errorDescription = error?.description?.toString() ?: "Failed to load page"
                                    }
                                }

                                override fun onReceivedSslError(
                                    view: WebView?,
                                    handler: SslErrorHandler?,
                                    error: SslError?
                                ) {
                                    handler?.proceed()
                                }

                                override fun shouldInterceptRequest(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): WebResourceResponse? {
                                    // 1. Layered AdBlock filtering
                                    val blockedResponse = adBlockEngine.handleInterceptRequest(request)
                                    if (blockedResponse != null) {
                                        return blockedResponse
                                    }

                                    // 2. Video stream detection
                                    val url = request?.url?.toString() ?: return null
                                    if (url.contains(".m3u8") || url.endsWith(".mp4")) {
                                        latestDetectedMediaUrl = url
                                    }

                                    return super.shouldInterceptRequest(view, request)
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val uri = request?.url ?: return false
                                    val url = uri.toString()
                                    val scheme = uri.scheme?.lowercase() ?: ""

                                    // AdBlock & Redirect Protection
                                    if (adBlockEngine.shouldBlockNavigation(url, request.isRedirect)) {
                                        Log.d("123Movies", "Blocked navigation/redirect: $url")
                                        return true
                                    }

                                    if (scheme == "http" || scheme == "https") {
                                        return false
                                    }

                                    return try {
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                        true
                                    } catch (e: Exception) {
                                        Log.e("WebView", "Cannot open external intent: $url", e)
                                        true
                                    }
                                }

                                override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                                    super.doUpdateVisitedHistory(view, url, isReload)
                                    canGoBack = view?.canGoBack() == true
                                    canGoForward = view?.canGoForward() == true
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    loadProgress = newProgress
                                    if (newProgress >= 100) {
                                        isLoading = false
                                    }
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    super.onReceivedTitle(view, title)
                                    if (!title.isNullOrBlank()) {
                                        pageTitle = title
                                    }
                                }

                                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                    if (view != null && callback != null) {
                                        isFullScreen = true
                                        onShowCustomView(view, callback)
                                    }
                                }

                                override fun onHideCustomView() {
                                    isFullScreen = false
                                    onHideCustomView()
                                }

                                override fun getDefaultVideoPoster(): Bitmap? {
                                    return BitmapFactory.decodeResource(ctx.resources, android.R.drawable.ic_media_play)
                                }

                                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                    return super.onConsoleMessage(consoleMessage)
                                }

                                override fun onCreateWindow(
                                    view: WebView?,
                                    isDialog: Boolean,
                                    isUserGesture: Boolean,
                                    resultMsg: android.os.Message?
                                ): Boolean {
                                    val newWebView = WebView(view?.context ?: return false)
                                    newWebView.settings.javaScriptEnabled = true
                                    newWebView.webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean {
                                            val url = request?.url?.toString() ?: return false
                                            if (adBlockEngine.shouldBlockUrl(url, request.url.host)) {
                                                return true
                                            }
                                            webView?.loadUrl(url)
                                            return true
                                        }
                                    }
                                    val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                                    transport.webView = newWebView
                                    resultMsg.sendToTarget()
                                    return true
                                }
                            }

                            loadUrl(DEFAULT_HOME_URL)
                        }
                        webView = wv
                        wv
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Sleek loading progress bar at the top of the app
                if (isLoading && loadProgress in 1..99) {
                    LinearProgressIndicator(
                        progress = { loadProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.TopCenter),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Error Screen if page failed to load
                if (hasError) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Error",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Failed to load webpage",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorDescription.ifEmpty { "Please check your internet connection or try again." },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        hasError = false
                                        isLoading = true
                                        webView?.reload()
                                    }
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Retry")
                                }
                                OutlinedButton(
                                    onClick = {
                                        hasError = false
                                        isLoading = true
                                        webView?.loadUrl(DEFAULT_HOME_URL)
                                    }
                                ) {
                                    Icon(Icons.Filled.Home, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Home")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating 3-Dot Button at Bottom-Right Corner (Hides when movie is clicked / playing / fullscreen)
        Floating3DotMenu(
            isVisible = isFloatingMenuVisible,
            hasUpdateBadge = latestAvailableUpdate?.isNewer == true,
            onNavigate = { destination ->
                currentScreen = destination
            },
            modifier = Modifier.align(Alignment.BottomEnd)
        )

        // Full-Screen Pages Animated Transitions
        AnimatedVisibility(
            visible = currentScreen != AppDestination.NONE,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(250)) + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (currentScreen) {
                    AppDestination.PROFILE -> {
                        ProfileScreen(onBack = { currentScreen = AppDestination.NONE })
                    }
                    AppDestination.UPDATES -> {
                        UpdatesScreen(onBack = { currentScreen = AppDestination.NONE })
                    }
                    AppDestination.SETTINGS -> {
                        SettingsScreen(onBack = { currentScreen = AppDestination.NONE })
                    }
                    AppDestination.FEEDBACK -> {
                        FeedbackScreen(onBack = { currentScreen = AppDestination.NONE })
                    }
                    AppDestination.NONE -> Unit
                }
            }
        }

        // Startup Update Prompt Modal (Shows right when app is launched if a newer release exists)
        if (showStartupUpdateModal && latestAvailableUpdate != null) {
            UpdatePromptModal(
                updateInfo = latestAvailableUpdate!!,
                onViewUpdate = {
                    showStartupUpdateModal = false
                    currentScreen = AppDestination.UPDATES
                },
                onDismiss = {
                    showStartupUpdateModal = false
                }
            )
        }
    }
}
