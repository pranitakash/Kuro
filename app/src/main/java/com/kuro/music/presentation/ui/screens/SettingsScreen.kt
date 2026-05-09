package com.kuro.music.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kuro.music.presentation.ui.theme.KuroError
import com.kuro.music.presentation.ui.theme.KuroOnBackground
import com.kuro.music.presentation.ui.theme.KuroOnSurfaceVariant
import com.kuro.music.presentation.ui.theme.KuroPrimary
import com.kuro.music.presentation.ui.theme.KuroSurfaceVariant
import com.kuro.music.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showStreamQualityDialog by remember { mutableStateOf(false) }
    var showDownloadQualityDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = KuroOnBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        // ─── Playback ───
        SettingsSectionHeader(icon = Icons.Filled.MusicNote, title = "Playback")

        SettingsClickItem(
            title = "Streaming Quality",
            subtitle = state.streamingQuality.replaceFirstChar { it.uppercase() },
            onClick = { showStreamQualityDialog = true }
        )
        SettingsClickItem(
            title = "Download Quality",
            subtitle = state.downloadQuality.replaceFirstChar { it.uppercase() },
            onClick = { showDownloadQualityDialog = true }
        )
        SettingsToggleItem(
            title = "Normalize Volume",
            subtitle = "Equalize loudness across tracks",
            checked = state.normalizeVolume,
            onToggle = { viewModel.setNormalizeVolume(it) }
        )
        SettingsToggleItem(
            title = "Gapless Playback",
            subtitle = "Seamless transitions between tracks",
            checked = state.gaplessPlayback,
            onToggle = { viewModel.setGaplessPlayback(it) }
        )

        // Crossfade
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Crossfade",
                fontSize = 15.sp,
                color = KuroOnBackground
            )
            Text(
                text = if (state.crossfadeDuration == 0) "Off" else "${state.crossfadeDuration}s",
                fontSize = 13.sp,
                color = KuroOnSurfaceVariant
            )
            Slider(
                value = state.crossfadeDuration.toFloat(),
                onValueChange = { viewModel.setCrossfadeDuration(it.toInt()) },
                valueRange = 0f..12f,
                steps = 11,
                colors = SliderDefaults.colors(
                    thumbColor = KuroPrimary,
                    activeTrackColor = KuroPrimary,
                    inactiveTrackColor = KuroSurfaceVariant
                )
            )
        }

        SettingsDivider()

        // ─── Interface ───
        SettingsSectionHeader(icon = Icons.Filled.BrightnessMedium, title = "Interface")

        SettingsClickItem(
            title = "Theme",
            subtitle = state.themeMode.replaceFirstChar { it.uppercase() },
            onClick = { showThemeDialog = true }
        )

        SettingsDivider()

        // ─── Source ───
        SettingsSectionHeader(icon = Icons.Filled.Code, title = "Source")

        SettingsClickItem(
            title = "Piped Instance",
            subtitle = state.pipedInstance,
            onClick = { }
        )
        SettingsToggleItem(
            title = "yt-dlp Fallback",
            subtitle = "Use yt-dlp when other methods fail",
            checked = state.ytDlpFallback,
            onToggle = { viewModel.setYtDlpFallback(it) }
        )

        SettingsDivider()

        // ─── Privacy ───
        SettingsSectionHeader(icon = Icons.Filled.Security, title = "Privacy")

        SettingsToggleItem(
            title = "Save Search History",
            subtitle = "Remember your searches",
            checked = state.saveSearchHistory,
            onToggle = { viewModel.setSaveSearchHistory(it) }
        )
        SettingsToggleItem(
            title = "Save Playback History",
            subtitle = "Track what you listen to",
            checked = state.savePlayHistory,
            onToggle = { viewModel.setSavePlayHistory(it) }
        )
        SettingsClickItem(
            title = "Clear Search History",
            subtitle = "Remove all search history",
            onClick = { showClearConfirmDialog = "search" }
        )
        SettingsClickItem(
            title = "Clear Playback History",
            subtitle = "Remove all playback history",
            onClick = { showClearConfirmDialog = "playback" }
        )

        SettingsDivider()

        // ─── Storage ───
        SettingsSectionHeader(icon = Icons.Filled.Storage, title = "Storage")

        SettingsClickItem(
            title = "Clear Cache",
            subtitle = "Free up cached data",
            onClick = { showClearConfirmDialog = "cache" }
        )

        SettingsDivider()

        // ─── About ───
        SettingsSectionHeader(icon = Icons.Filled.Info, title = "About")

        SettingsClickItem(title = "Kuro Music", subtitle = "Version 1.0.0", onClick = {})
        SettingsClickItem(title = "Source Code", subtitle = "github.com/kuro-music", onClick = {})
        SettingsClickItem(title = "Open Source Licenses", subtitle = "Third-party libraries", onClick = {})

        Spacer(modifier = Modifier.height(80.dp))
    }

    // ─── Dialogs ───
    if (showThemeDialog) {
        val themes = listOf("system", "light", "dark", "amoled")
        ChoiceDialog(
            title = "Theme",
            options = themes.map { it.replaceFirstChar { c -> c.uppercase() } },
            selectedIndex = themes.indexOf(state.themeMode),
            onSelected = { index ->
                viewModel.setThemeMode(themes[index])
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showStreamQualityDialog) {
        val qualities = listOf("low", "medium", "high")
        val labels = listOf("Low (64 kbps)", "Medium (128 kbps)", "High (320 kbps)")
        ChoiceDialog(
            title = "Streaming Quality",
            options = labels,
            selectedIndex = qualities.indexOf(state.streamingQuality),
            onSelected = { index ->
                viewModel.setStreamingQuality(qualities[index])
                showStreamQualityDialog = false
            },
            onDismiss = { showStreamQualityDialog = false }
        )
    }

    if (showDownloadQualityDialog) {
        val qualities = listOf("low", "medium", "high")
        val labels = listOf("Low (64 kbps)", "Medium (128 kbps)", "High (320 kbps)")
        ChoiceDialog(
            title = "Download Quality",
            options = labels,
            selectedIndex = qualities.indexOf(state.downloadQuality),
            onSelected = { index ->
                viewModel.setDownloadQuality(qualities[index])
                showDownloadQualityDialog = false
            },
            onDismiss = { showDownloadQualityDialog = false }
        )
    }

    showClearConfirmDialog?.let { type ->
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = null },
            title = { Text("Clear ${type.replaceFirstChar { it.uppercase() }}?", fontWeight = FontWeight.Bold) },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    when (type) {
                        "search" -> viewModel.clearSearchHistory()
                        "playback" -> viewModel.clearPlayHistory()
                        "cache" -> viewModel.clearCaches()
                    }
                    showClearConfirmDialog = null
                }) {
                    Text("Clear", color = KuroError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = null }) { Text("Cancel") }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = KuroOnSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = KuroOnSurfaceVariant,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SettingsClickItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = KuroOnBackground)
            Text(text = subtitle, fontSize = 13.sp, color = KuroOnSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = KuroOnBackground)
            Text(text = subtitle, fontSize = 13.sp, color = KuroOnSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = KuroPrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = KuroSurfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        color = KuroSurfaceVariant
    )
}

@Composable
private fun ChoiceDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(index) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = index == selectedIndex,
                            onClick = { onSelected(index) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = KuroPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option, fontSize = 15.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        containerColor = Color.White
    )
}
