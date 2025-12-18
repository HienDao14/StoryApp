package com.hiendao.coreui.theme

import androidx.compose.ui.graphics.Color

fun Color.mix(
    color: Color,
    fraction: Float,
) = Color(
    red = red * fraction + color.red * (1f - fraction),
    green = green * fraction + color.green * (1f - fraction),
    blue = blue * fraction + color.blue * (1f - fraction),
    alpha = alpha * fraction + color.alpha * (1f - fraction),
)


