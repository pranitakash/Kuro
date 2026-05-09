package com.kuro.music

import android.app.Application
import com.kuro.music.data.remote.WebViewStreamExtractor
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class KuroApplication : Application() {

    @Inject
    lateinit var webViewExtractor: WebViewStreamExtractor

    override fun onCreate() {
        super.onCreate()
        // Pre-warm the WebView engine at app startup.
        // First WebView creation on Android initializes the entire WebKit engine (~2-5s).
        // By warming up here, it's ready by the time the user taps a song.
        webViewExtractor.warmUp()
    }
}
