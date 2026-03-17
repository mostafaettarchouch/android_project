package com.ofppt.istak.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.neumorphic(
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    elevation: Dp = 6.dp,
    lightShadowColor: Color = NeumorphicColors.lightShadow(),
    darkShadowColor: Color = NeumorphicColors.darkShadow(),
    isPressed: Boolean = false
): Modifier {
    val backgroundColor = MaterialTheme.colorScheme.surface

    return this.then(
        Modifier
            .drawBehind {
                val shadowWidth = elevation.toPx()
                val blurRadius = shadowWidth * 1.5f

                drawIntoCanvas { canvas ->
                    val paint = Paint()
                    val frameworkPaint = paint.asFrameworkPaint()
                    
                    val left = 0f
                    val top = 0f
                    val right = size.width
                    val bottom = size.height
                    
                    val radiusX = shape.topStart.toPx(size, this)
                    val radiusY = shape.topStart.toPx(size, this)

                    if (!isPressed) {
                        // Dark Shadow (Bottom-Right)
                        frameworkPaint.color = darkShadowColor.toArgb()
                        frameworkPaint.setShadowLayer(
                            blurRadius,
                            shadowWidth,
                            shadowWidth,
                            darkShadowColor.toArgb()
                        )
                        canvas.drawRoundRect(
                            left, top, right, bottom,
                            radiusX, radiusY,
                            paint
                        )

                        // Light Shadow (Top-Left)
                        frameworkPaint.color = lightShadowColor.toArgb()
                        frameworkPaint.setShadowLayer(
                            blurRadius,
                            -shadowWidth,
                            -shadowWidth,
                            lightShadowColor.toArgb()
                        )
                        canvas.drawRoundRect(
                            left, top, right, bottom,
                            radiusX, radiusY,
                            paint
                        )
                    }
                }
            }
            .background(backgroundColor, shape)
    )
}

// Specific colors for Neumorphism that work well with the existing palette
object NeumorphicColors {
    @Composable
    fun background() = MaterialTheme.colorScheme.background

    @Composable
    fun surface() = MaterialTheme.colorScheme.surface
    
    @Composable
    fun lightShadow() = if (MaterialTheme.colorScheme.background == Slate50) 
        Color.White 
    else 
        Color(0xFF334155).copy(alpha = 0.3f) // Slate 700ish

    @Composable
    fun darkShadow() = if (MaterialTheme.colorScheme.background == Slate50) 
        Color(0xFFD1D9E6).copy(alpha = 0.7f)
    else 
        Color.Black.copy(alpha = 0.6f)
}
