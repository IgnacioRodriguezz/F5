package com.rodriguez.f5.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = Lime,
    onPrimary = Ink,
    secondary = Amber,
    onSecondary = Ink,
    error = Coral,
    onError = Ink,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceAlternate,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = Lime,
    onPrimary = Ink,
    secondary = Amber,
    onSecondary = Ink,
    error = Coral,
    onError = Ink,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceAlternate,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline
)

/**
 * Typography.
 *
 * Condensed carries the display roles, where the narrow width is what lets a
 * scoreline or a long club name fit in one line. Sans carries everything a
 * person actually reads at small size.
 */
private val F5Typography = Typography(
    displayLarge = TextStyle(fontFamily = Condensed, fontSize = 40.sp, fontWeight = FontWeight.Bold),
    displayMedium = TextStyle(fontFamily = Condensed, fontSize = 52.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontFamily = Condensed, fontSize = 36.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontFamily = Condensed, fontSize = 32.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontFamily = Condensed, fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontFamily = Sans, fontSize = 15.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontFamily = Sans, fontSize = 14.sp, fontWeight = FontWeight.Medium),
    bodyMedium = TextStyle(fontFamily = Sans, fontSize = 14.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontFamily = Sans, fontSize = 13.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontFamily = Sans, fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontFamily = Sans, fontSize = 12.sp, fontWeight = FontWeight.Bold)
)

private val LocalF5Colors = staticCompositionLocalOf { darkF5Colors }

@Composable
fun F5Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalF5Colors provides if (darkTheme) darkF5Colors else lightF5Colors
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = F5Typography,
            content = content
        )
    }
}

/** Access point for the colours Material has no slot for. */
object F5Theme {
    val colors: F5Colors
        @Composable
        @ReadOnlyComposable
        get() = LocalF5Colors.current
}
