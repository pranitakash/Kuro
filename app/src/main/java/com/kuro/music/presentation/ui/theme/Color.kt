package com.kuro.music.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Monochromatic Light Theme ───────────────────────────────────────────────
// Clean black & white aesthetic inspired by modern music app design

// Primary = Black (used for buttons, active states, accents)
val KuroPrimary = Color(0xFF1A1A1A)
val KuroOnPrimary = Color(0xFFFFFFFF)

// Backgrounds
val KuroBackground = Color(0xFFFFFFFF)
val KuroSurface = Color(0xFFFFFFFF)
val KuroSurfaceVariant = Color(0xFFF2F2F7)        // Light gray for cards, search bars
val KuroSurfaceContainer = Color(0xFFE8E8ED)       // Slightly darker for pressed states

// Text colors
val KuroOnBackground = Color(0xFF1A1A1A)           // Primary text — black
val KuroOnSurface = Color(0xFF1A1A1A)              // Primary text — black
val KuroOnSurfaceVariant = Color(0xFF8E8E93)       // Secondary text — medium gray

// Outline & dividers
val KuroOutline = Color(0xFFD1D1D6)
val KuroOutlineVariant = Color(0xFFE5E5EA)

// Error / Like
val KuroError = Color(0xFFFF3B30)                  // Red for errors & liked state

// Mini player (dark floating capsule)
val KuroMiniPlayerBg = Color(0xFF1A1A1A)
val KuroMiniPlayerText = Color(0xFFFFFFFF)
val KuroMiniPlayerSecondary = Color(0xFFAEAEB2)

// Now Playing
val KuroNowPlayingBg = Color(0xFFFAFAFA)

// Chip colors
val KuroChipSelected = Color(0xFF1A1A1A)           // Black when selected
val KuroChipSelectedText = Color(0xFFFFFFFF)        // White text on selected
val KuroChipUnselected = Color(0xFFF2F2F7)          // Light gray when unselected
val KuroChipUnselectedText = Color(0xFF636366)      // Dark gray text on unselected
