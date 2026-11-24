package com.rodriguez.f5.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.rodriguez.f5.R

/**
 * The two faces of the design.
 *
 * Barlow Condensed is the display face: it carries titles, scores and any
 * large number, where its narrow width lets a scoreline sit in one line.
 * Sora is everything else, because a neutral sans stays legible at the small
 * sizes a results table needs.
 *
 * Both are bundled as files rather than fetched at runtime, so the app looks
 * the same with no network and on a device without Play Services.
 */

val Condensed = FontFamily(
    Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_condensed_bold, FontWeight.Bold)
)

val Sans = FontFamily(
    Font(R.font.sora_regular, FontWeight.Normal),
    Font(R.font.sora_medium, FontWeight.Medium),
    Font(R.font.sora_bold, FontWeight.Bold)
)
