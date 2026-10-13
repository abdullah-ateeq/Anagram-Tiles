package com.example.anagramtiles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.sp
import com.example.anagramtiles.data.AppTheme
import com.example.anagramtiles.data.Difficulty
import com.example.anagramtiles.data.TimerOption
import com.example.anagramtiles.data.UserSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onSettingsChanged: (UserSettings) -> Unit,
    onResetSettings: () -> Unit,
    onResetStatistics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showResetStatsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "SETTINGS",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Appearance Section
        SectionTitle(title = "APPEARANCE")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTheme.entries.forEach { theme ->
                        FilterChip(
                            selected = settings.theme == theme,
                            onClick = { onSettingsChanged(settings.copy(theme = theme)) },
                            label = { Text(theme.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                SwitchSettingRow(
                    title = "Dynamic Color",
                    subtitle = "Use Material You colors on supported devices",
                    checked = settings.dynamicColor,
                    onCheckedChange = { onSettingsChanged(settings.copy(dynamicColor = it)) }
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                SwitchSettingRow(
                    title = "Animations",
                    subtitle = "Enable tile movements and transitions",
                    checked = settings.animations,
                    onCheckedChange = { onSettingsChanged(settings.copy(animations = it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Gameplay Section
        SectionTitle(title = "GAMEPLAY")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CPU Difficulty",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Difficulty.entries.forEach { diff ->
                        FilterChip(
                            selected = settings.cpuDifficulty == diff,
                            onClick = { onSettingsChanged(settings.copy(cpuDifficulty = diff)) },
                            label = { Text(diff.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Round Timer",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimerOption.entries.forEach { opt ->
                        FilterChip(
                            selected = settings.timer == opt,
                            onClick = { onSettingsChanged(settings.copy(timer = opt)) },
                            label = {
                                Text(
                                    when (opt) {
                                        TimerOption.OFF -> "Off"
                                        TimerOption.SECONDS_30 -> "30s"
                                        TimerOption.SECONDS_60 -> "60s"
                                        TimerOption.SECONDS_90 -> "90s"
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                SwitchSettingRow(
                    title = "Word Feedback",
                    subtitle = "Show validation notices after checking",
                    checked = settings.showWordFeedback,
                    onCheckedChange = { onSettingsChanged(settings.copy(showWordFeedback = it)) }
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                SwitchSettingRow(
                    title = "Confirm Restart",
                    subtitle = "Prompt before restarting an active match",
                    checked = settings.confirmRestart,
                    onCheckedChange = { onSettingsChanged(settings.copy(confirmRestart = it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sound & Haptics Section
        SectionTitle(title = "FEEDBACK")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SwitchSettingRow(
                    title = "Sound Effects",
                    subtitle = "Audio cues for tile actions and word discovery",
                    checked = settings.sound,
                    onCheckedChange = { onSettingsChanged(settings.copy(sound = it)) }
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                SwitchSettingRow(
                    title = "Haptic Feedback",
                    subtitle = "Tactile device vibrations on tap and swap",
                    checked = settings.hapticFeedback,
                    onCheckedChange = { onSettingsChanged(settings.copy(hapticFeedback = it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reset Buttons
        OutlinedButton(
            onClick = { showResetStatsDialog = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Reset Statistics", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { showResetSettingsDialog = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Reset Settings to Default", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Reset Statistics Dialog
    if (showResetStatsDialog) {
        AlertDialog(
            onDismissRequest = { showResetStatsDialog = false },
            title = { Text("Reset Statistics", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear all your saved statistics and streaks? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetStatistics()
                        showResetStatsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetStatsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Settings Dialog
    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings", fontWeight = FontWeight.Bold) },
            text = { Text("Restore all settings to their default configuration?") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetSettings()
                        showResetSettingsDialog = false
                    }
                ) {
                    Text("Restore", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SwitchSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
