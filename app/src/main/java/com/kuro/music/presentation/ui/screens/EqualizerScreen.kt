package com.kuro.music.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kuro.music.presentation.ui.theme.KuroOnBackground
import com.kuro.music.presentation.ui.theme.KuroOnSurfaceVariant
import com.kuro.music.presentation.ui.theme.KuroPrimary
import com.kuro.music.presentation.ui.theme.KuroSurfaceVariant
import com.kuro.music.presentation.viewmodel.EqualizerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Equalizer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = KuroOnBackground
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = KuroOnBackground
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Enable/Disable toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Equalizer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = KuroOnBackground
                )
                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = { viewModel.toggleEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = KuroPrimary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = KuroSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets
            Text(
                text = "Presets",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = KuroOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            val chunkedPresets = state.presets.chunked(3)
            chunkedPresets.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEachIndexed { _, preset ->
                        val index = state.presets.indexOf(preset)
                        FilterChip(
                            selected = state.selectedPresetIndex == index,
                            onClick = {
                                if (state.isEnabled) viewModel.selectPreset(index)
                            },
                            label = { Text(preset, fontSize = 12.sp) },
                            enabled = state.isEnabled,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KuroPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = KuroSurfaceVariant,
                                labelColor = KuroOnSurfaceVariant
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Band sliders
            Text(
                text = "Band Levels",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = KuroOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (state.bandFrequencies.isNotEmpty() && state.bandLevels.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    state.bandFrequencies.forEachIndexed { index, freq ->
                        if (index < state.bandLevels.size) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${state.bandLevels[index] / 100}dB",
                                    fontSize = 10.sp,
                                    color = KuroOnSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Slider(
                                    value = state.bandLevels[index].toFloat(),
                                    onValueChange = { value ->
                                        if (state.isEnabled) {
                                            viewModel.setBandLevel(index, value.toInt().toShort())
                                        }
                                    },
                                    valueRange = state.minBandLevel.toFloat()..state.maxBandLevel.toFloat(),
                                    enabled = state.isEnabled,
                                    colors = SliderDefaults.colors(
                                        thumbColor = KuroPrimary,
                                        activeTrackColor = KuroPrimary,
                                        inactiveTrackColor = KuroSurfaceVariant
                                    ),
                                    modifier = Modifier.width(80.dp)
                                )
                                Text(
                                    text = formatFrequency(freq),
                                    fontSize = 10.sp,
                                    color = KuroOnSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bass Boost
            Text(
                text = "Bass Boost",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = KuroOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Off", fontSize = 11.sp, color = KuroOnSurfaceVariant)
                Slider(
                    value = state.bassBoostStrength.toFloat(),
                    onValueChange = {
                        if (state.isEnabled) viewModel.setBassBoost(it.toInt())
                    },
                    valueRange = 0f..1000f,
                    enabled = state.isEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = KuroPrimary,
                        activeTrackColor = KuroPrimary,
                        inactiveTrackColor = KuroSurfaceVariant
                    )
                )
                Text(text = "Max", fontSize = 11.sp, color = KuroOnSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private fun formatFrequency(hz: Int): String {
    return if (hz >= 1000) "${hz / 1000}kHz" else "${hz}Hz"
}
