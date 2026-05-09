package com.kuro.music.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kuro.music.presentation.ui.theme.GothamFontFamily
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
            .statusBarsPadding()
    ) {
        // ─── Large "Settings" title ───
        Text(
            text = "Settings",
            fontSize = 32.sp,
            fontFamily = GothamFontFamily,
            fontWeight = FontWeight.Bold,
            color = KuroOnBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        // ─── Profile row ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(KuroSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = KuroOnSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "kuro",
                    fontSize = 17.sp,
                    fontFamily = GothamFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = KuroOnBackground
                )
                Text(
                    text = "View profile",
                    fontSize = 13.sp,
                    fontFamily = GothamFontFamily,
                    color = KuroOnSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = KuroOnSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ─── Account section ───
        SettingsSectionTitle("Account")
        SettingsRow("Account settings") { }
        SettingsRow("Manage subscription") { }
        SettingsRow("Restore purchases") { }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Preferences section ───
        SettingsSectionTitle("Preferences")
        SettingsRow("Audio quality") { showStreamQualityDialog = true }
        SettingsRow("Playback") { showThemeDialog = true }
        SettingsRow("Downloads") { showDownloadQualityDialog = true }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Support section ───
        SettingsSectionTitle("Support")
        SettingsRow("Help center") { }
        SettingsRow("Feedback") { }

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
            title = {
                Text(
                    "Clear ${type.replaceFirstChar { it.uppercase() }}?",
                    fontFamily = GothamFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text("This action cannot be undone.", fontFamily = GothamFontFamily) },
            confirmButton = {
                TextButton(onClick = {
                    when (type) {
                        "search" -> viewModel.clearSearchHistory()
                        "playback" -> viewModel.clearPlayHistory()
                        "cache" -> viewModel.clearCaches()
                    }
                    showClearConfirmDialog = null
                }) {
                    Text("Clear", color = KuroError, fontFamily = GothamFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = null }) {
                    Text("Cancel", fontFamily = GothamFontFamily)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontFamily = GothamFontFamily,
        fontWeight = FontWeight.Bold,
        color = KuroOnBackground,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontFamily = GothamFontFamily,
            color = KuroOnBackground
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = KuroOnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
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
        title = { Text(title, fontFamily = GothamFontFamily, fontWeight = FontWeight.Bold) },
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
                        Text(text = option, fontSize = 15.sp, fontFamily = GothamFontFamily)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", fontFamily = GothamFontFamily) }
        },
        containerColor = Color.White
    )
}
