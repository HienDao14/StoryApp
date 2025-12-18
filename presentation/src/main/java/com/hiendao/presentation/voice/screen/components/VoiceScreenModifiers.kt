package com.hiendao.presentation.voice.screen.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.progressBorder(
    progress: Float,
    color: Color,
    strokeWidth: Dp = 4.dp
): Modifier = composed {
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    
    // Animate progress for smoothness
    val animatedProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), label = "borderProgress")

    this.drawWithCache {
        val path = Path()
        // Define path: Top-Center -> Top-Right -> Bottom-Right -> Bottom-Left -> Top-Left -> Top-Center
        // Or simple rectangle perimeter
        val w = size.width
        val h = size.height
        
        // Start from top-left for simplicity, or top-center
        // Let's do Top-Left clockwise
        path.moveTo(0f, 0f)
        path.lineTo(w, 0f)
        path.lineTo(w, h)
        path.lineTo(0f, h)
        path.lineTo(0f, 0f)
        
        // Or creating a rounded rect path based on shape?
        // Simpler implementation: Check rect perimeter
        
        val pathMeasure = PathMeasure()
        pathMeasure.setPath(path, false)
        val length = pathMeasure.length
        
        val partialPath = Path()
        pathMeasure.getSegment(0f, length * animatedProgress, partialPath, true)

        onDrawWithContent {
            drawContent()
            drawPath(
                path = partialPath,
                color = color,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
    }
}
