package com.ofppt.istak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun ProfileAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    onClick: () -> Unit = {}
) {
    val initial = remember(name) {
        if (name.isNotBlank()) name.first().toString().uppercase(Locale.ROOT) else "?"
    }

    val backgroundColor = remember(name) {
        generateColor(name)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontSize = (size.value / 2).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun generateColor(name: String): Color {
    val colors = listOf(
        Color(0xFFF44336), // Red
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF673AB7), // Deep Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF2196F3), // Blue
        Color(0xFF03A9F4), // Light Blue
        Color(0xFF00BCD4), // Cyan
        Color(0xFF009688), // Teal
        Color(0xFF4CAF50), // Green
        Color(0xFF8BC34A), // Light Green
        Color(0xFFCDDC39), // Lime
        Color(0xFFFFC107), // Amber
        Color(0xFFFF9800), // Orange
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF795548), // Brown
        Color(0xFF607D8B)  // Blue Grey
    )
    
    val hash = name.hashCode()
    val index = Math.abs(hash) % colors.size
    return colors[index]
}
