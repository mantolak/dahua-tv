package com.dahuatv.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val Colors = darkColorScheme(
    primary = Color(0xFF2E7DD7),
    background = Color.Black,
    surface = Color(0xFF101418),
)

@Composable
fun DahuaTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
