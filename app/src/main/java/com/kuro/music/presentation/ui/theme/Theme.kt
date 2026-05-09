package com.kuro.music.presentation.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val KuroColorScheme = lightColorScheme(
    primary = KuroPrimary,
    onPrimary = KuroOnPrimary,
    secondary = KuroPrimary,
    onSecondary = KuroOnPrimary,
    tertiary = KuroPrimary,
    onTertiary = KuroOnPrimary,
    background = KuroBackground,
    onBackground = KuroOnBackground,
    surface = KuroSurface,
    onSurface = KuroOnSurface,
    surfaceVariant = KuroSurfaceVariant,
    onSurfaceVariant = KuroOnSurfaceVariant,
    outline = KuroOutline,
    outlineVariant = KuroOutlineVariant,
    error = KuroError,
    onError = Color.White,
    errorContainer = KuroError.copy(alpha = 0.12f),
    onErrorContainer = KuroError,
)

@Composable
fun KuroTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            // Light status bar icons (dark icons on light background)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = KuroColorScheme,
        typography = KuroTypography,
        content = content
    )
}
