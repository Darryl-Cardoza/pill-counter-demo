package com.example.pillcountingnewmodels.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// Light color scheme
private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
)

// Dark color scheme
//Currently app is configured only for light theme
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
)

@Composable
fun PillCountingNewModelsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    lightColors: ColorScheme = LightColorScheme,
    darkColors: ColorScheme = DarkColorScheme,
    lightExtendedColors: ExtendedColors,
    darkExtendedColors: ExtendedColors,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColors else lightColors
    val extendedColors = if (darkTheme) darkExtendedColors else lightExtendedColors

    // Apply MaterialTheme
    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object AppTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
