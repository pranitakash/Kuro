package com.kuro.music.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class TimerOption(val label: String, val durationMs: Long)

@Composable
fun SleepTimerDialog(
    isTimerActive: Boolean,
    remainingFormatted: String,
    onStartTimer: (Long) -> Unit,
    onAfterCurrentSong: () -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        TimerOption("15 minutes", 15 * 60 * 1000L),
        TimerOption("30 minutes", 30 * 60 * 1000L),
        TimerOption("45 minutes", 45 * 60 * 1000L),
        TimerOption("1 hour", 60 * 60 * 1000L),
        TimerOption("1.5 hours", 90 * 60 * 1000L),
    )

    var selectedIndex by remember { mutableStateOf(-1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Filled.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = {
            Text(
                text = if (isTimerActive) "Sleep Timer Active" else "Sleep Timer",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                if (isTimerActive) {
                    Text(
                        text = "Remaining: $remainingFormatted",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCancelTimer(); onDismiss() }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Cancel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Cancel Timer",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    options.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedIndex = index }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option.label)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIndex = options.size }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedIndex == options.size,
                            onClick = { selectedIndex = options.size }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "After current song")
                    }
                }
            }
        },
        confirmButton = {
            if (!isTimerActive) {
                TextButton(
                    onClick = {
                        when {
                            selectedIndex in options.indices -> {
                                onStartTimer(options[selectedIndex].durationMs)
                                onDismiss()
                            }
                            selectedIndex == options.size -> {
                                onAfterCurrentSong()
                                onDismiss()
                            }
                        }
                    },
                    enabled = selectedIndex >= 0
                ) {
                    Text("Start")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
