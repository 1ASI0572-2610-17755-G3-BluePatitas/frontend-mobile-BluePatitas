package com.bluepatitas.mobile.core.designsystem.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PawLogoIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier.size(80.dp)) {
        val pad = size.minDimension / 12f
        drawCircle(color, radius = size.minDimension * 0.20f, center = Offset(size.width / 2f, size.height * 0.62f))
        drawCircle(color, radius = size.minDimension * 0.10f, center = Offset(size.width * 0.25f, size.height * 0.42f))
        drawCircle(color, radius = size.minDimension * 0.10f, center = Offset(size.width * 0.41f, size.height * 0.26f))
        drawCircle(color, radius = size.minDimension * 0.10f, center = Offset(size.width * 0.59f, size.height * 0.26f))
        drawCircle(color, radius = size.minDimension * 0.10f, center = Offset(size.width * 0.75f, size.height * 0.42f))
        drawArc(
            color = color,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(pad, pad),
            size = Size(size.width - pad * 2f, size.height - pad * 2f),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun NavigationDotIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier.size(22.dp)) {
        drawCircle(color = color, radius = size.minDimension * 0.26f, center = center)
    }
}
