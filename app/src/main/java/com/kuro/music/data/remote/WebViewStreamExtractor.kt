package com.kuro.music.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves audio stream URLs by loading YouTube's embed player in a pre-warmed,
 * reusable hidden WebView and intercepting the actual media requests.
 *
 * Key optimizations:
 * - WebView is pre-warmed at startup (eliminates 2-5s cold start)
 * - Single WebView is reused across extractions (no re-creation overhead)
 * - Extraction mutex ensures serialized access to the shared WebView
 */
@Singleton
class WebViewStreamExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "WebViewExtractor"
        private const val TIMEOUT_MS = 10000L // 10s (was 15s)
    }

    private var webView: WebView? = null
    private var currentResult: CompletableDeferred<String>? = null
    private val extractionMutex = Mutex()
    private val mainHandler = Handler(Looper.getMainLooper())

    // Reusable client that intercepts audio URLs
    private val interceptClient = object : WebViewClient() {
        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            val url = request?.url?.toString() ?: return null

            // Intercept audio requests to googlevideo.com
            if (url.contains("googlevideo.com") &&
                (url.contains("mime=audio") || url.contains("mime%3Daudio"))) {
                Log.d(TAG, "Intercepted audio URL!")
                currentResult?.let { result ->
                    if (!result.isCompleted) {
                        // Clean the URL — remove range parameter to get full audio
                        val cleanUrl = url
                            .replace(Regex("&range=[^&]+"), "")
                            .replace(Regex("&rn=[^&]+"), "")
                        result.complete(cleanUrl)
                    }
                }
                return null
            }
            return null
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d(TAG, "Page loaded: $url")

            // Aggressively auto-click play — try multiple times with delays
            view?.evaluateJavascript("""
                (function() {
                    function tryPlay() {
                        var playBtn = document.querySelector('.ytp-play-button');
                        if (playBtn) playBtn.click();
                        var bigPlay = document.querySelector('.ytp-large-play-button');
                        if (bigPlay) bigPlay.click();
                        var video = document.querySelector('video');
                        if (video) { video.muted = false; video.play(); }
                    }
                    tryPlay();
                    setTimeout(tryPlay, 300);
                    setTimeout(tryPlay, 800);
                })();
            """.trimIndent(), null)
        }
    }

    /**
     * Pre-warm the WebView so first extraction is fast.
     * Call this from Application.onCreate() or early in the app lifecycle.
     */
    fun warmUp() {
        mainHandler.post {
            if (webView == null) {
                Log.d(TAG, "Pre-warming WebView...")
                webView = createWebView()
                // Load a blank page to fully initialize the WebView engine
                webView?.loadUrl("about:blank")
                Log.d(TAG, "WebView pre-warmed")
            }
        }
    }

    /**
     * Load a YouTube embed page and intercept the audio stream URL.
     * Uses a pre-warmed, reusable WebView for instant startup.
     */
    suspend fun extractAudioUrl(videoId: String): String {
        // Serialize extractions — only one at a time since we share the WebView
        extractionMutex.withLock {
            val result = CompletableDeferred<String>()
            currentResult = result

            withContext(Dispatchers.Main) {
                val wv = webView ?: createWebView().also { webView = it }
                // Stop any previous load
                wv.stopLoading()

                val embedUrl = "https://www.youtube.com/embed/$videoId?autoplay=1&fs=0&controls=0&rel=0&modestbranding=1"
                Log.d(TAG, "Loading embed: $embedUrl")
                wv.loadUrl(embedUrl)
            }

            val url = withTimeoutOrNull(TIMEOUT_MS) {
                result.await()
            }

            currentResult = null

            if (url == null) {
                throw Exception("Timed out waiting for audio stream URL")
            }

            Log.d(TAG, "Extracted audio URL: ${url.take(100)}...")
            return url
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView {
        val wv = WebView(context)

        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            // Block images to speed up page load — we only need audio
            blockNetworkImage = true
            loadsImagesAutomatically = false
            userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
        }

        wv.webViewClient = interceptClient
        return wv
    }
}
