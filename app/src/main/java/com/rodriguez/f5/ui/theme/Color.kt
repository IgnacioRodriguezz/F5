package com.rodriguez.f5.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Palette of the F5 design.
 *
 * Four brand colours plus neutrals that are pure mixes of #08080E and white,
 * with no other hue. Nothing outside this file knows a hex code.
 */

// Brand
val Ink = Color(0xFF08080E)
val Lime = Color(0xFFB6FF00)
val Amber = Color(0xFFFFBE5D)
val Coral = Color(0xFFFD5D5D)

// Light neutrals
val LightBackground = Color(0xFFF5F5F5)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceAlternate = Color(0xFFEEEEEE)
val LightTextPrimary = Ink
val LightTextSecondary = Color(0xFF525256)
val LightTextTertiary = Color(0xFF77777A)
val LightOutline = Color(0xFFE1E1E1)

// Dark neutrals
val DarkBackground = Ink
val DarkSurface = Color(0xFF19191F)
val DarkSurfaceAlternate = Color(0xFF26262B)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFA9A9AB)
val DarkTextTertiary = Color(0xFF88888B)
val DarkOutline = Color(0xFF343439)

/**
 * Colours the app needs that Material's scheme has no slot for.
 *
 * Exposed through a CompositionLocal so a composable reads them the same way
 * it reads [androidx.compose.material3.MaterialTheme.colorScheme].
 */
data class F5Colors(
    val zoneQualifies: Color,
    val zonePlayoff: Color,
    val zoneNeutral: Color,
    val zoneRelegated: Color,
    val statusLive: Color,
    val statusFinished: Color,
    val statusScheduled: Color,
    val statusPostponed: Color,
    val formWon: Color,
    val formDrawn: Color,
    val formLost: Color,
    val surfaceAlternate: Color,
    val textTertiary: Color,
    /** Crests carry their own colours, so they sit on a neutral white plate. */
    val crestPlate: Color,
    /** Text and icons drawn on top of any brand fill. */
    val onBrand: Color
)

val lightF5Colors = F5Colors(
    zoneQualifies = Lime,
    zonePlayoff = Amber,
    zoneNeutral = LightTextTertiary,
    zoneRelegated = Coral,
    statusLive = Coral,
    statusFinished = LightTextTertiary,
    statusScheduled = Lime,
    statusPostponed = Amber,
    formWon = Lime,
    formDrawn = LightTextTertiary,
    formLost = Coral,
    surfaceAlternate = LightSurfaceAlternate,
    textTertiary = LightTextTertiary,
    crestPlate = Color(0xFFFFFFFF),
    onBrand = Ink
)

val darkF5Colors = F5Colors(
    zoneQualifies = Lime,
    zonePlayoff = Amber,
    zoneNeutral = DarkTextTertiary,
    zoneRelegated = Coral,
    statusLive = Coral,
    statusFinished = DarkTextTertiary,
    statusScheduled = Lime,
    statusPostponed = Amber,
    formWon = Lime,
    formDrawn = DarkTextTertiary,
    formLost = Coral,
    surfaceAlternate = DarkSurfaceAlternate,
    textTertiary = DarkTextTertiary,
    crestPlate = Color(0xFFFFFFFF),
    onBrand = Ink
)
