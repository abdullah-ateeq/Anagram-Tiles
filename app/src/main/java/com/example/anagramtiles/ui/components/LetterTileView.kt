package com.example.anagramtiles.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anagramtiles.data.LetterTile
import com.example.anagramtiles.ui.theme.TileDarkBackground
import com.example.anagramtiles.ui.theme.TileDarkBorder
import com.example.anagramtiles.ui.theme.TileDarkText
import com.example.anagramtiles.ui.theme.TileLightBackground
import com.example.anagramtiles.ui.theme.TileLightBorder
import com.example.anagramtiles.ui.theme.TileLightText

@Composable
fun LetterTileView(
    tile: LetterTile,
    positionIndex: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val baseScale = if (isSelected) 1.07f else 1.0f
    val scaleAnim by animateFloatAsState(
        targetValue = baseScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tileScale"
    )

    val bgColor = if (isDark) TileDarkBackground else TileLightBackground
    val textColor = if (isDark) TileDarkText else TileLightText
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        if (isDark) TileDarkBorder else TileLightBorder
    }
    val elevation = if (isSelected) 8.dp else 3.dp
    val borderWidth = if (isSelected) 2.5.dp else 1.dp

    val accessibleDescription = if (isSelected) {
        "Letter ${tile.letter} selected, position ${positionIndex + 1}"
    } else {
        "Letter ${tile.letter}, position ${positionIndex + 1}"
    }

    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = bgColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
        modifier = modifier
            .scale(scaleAnim)
            .defaultMinSize(minWidth = 50.dp, minHeight = 60.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                contentDescription = accessibleDescription
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            Text(
                text = tile.letter.toString(),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
