package com.sistemapersonal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.sistemapersonal.model.AccentTheme

private val BaseDarkScheme = darkColorScheme(
    background = Bg0,
    surface = Bg1,
    onBackground = Ink0,
    onSurface = Ink0,
    primary = Amber,
    onPrimary = Void,
    secondary = Cyan,
    error = Danger
)

@Composable
fun SistemaPersonalTheme(moduloActivo: AccentTheme = AccentTheme.AMBER, content: @Composable () -> Unit) {
    val accent = accentFor(moduloActivo)
    val scheme = BaseDarkScheme.copy(primary = accent.accent, secondary = accent.accent)
    MaterialTheme(
        colorScheme = scheme,
        typography = SistemaPersonalTypography,
        content = content
    )
}
