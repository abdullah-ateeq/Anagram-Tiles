package com.example.anagramtiles.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anagramtiles.data.TimerOption

@Composable
fun TimerProgressView(
    timeRemaining: Int,
    timerOption: TimerOption,
    modifier: Modifier = Modifier
) {
    if (timerOption == TimerOption.OFF) return

    val totalSeconds = timerOption.seconds.coerceAtLeast(1)
    val fraction = (timeRemaining.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)

    val isUrgent = timeRemaining <= 10
    val progressColor by animateColorAsState(
        targetValue = if (isUrgent) Color(0xFFE53935) else MaterialTheme.colorScheme.primary,
        label = "timerColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TIME",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                letterSpacing = 1.sp
            )

            Text(
                text = "$timeRemaining s",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUrgent) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}
