package com.rodriguez.f5.ui.compose.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rodriguez.f5.domain.model.QualificationZone
import com.rodriguez.f5.domain.model.ZoneKind
import com.rodriguez.f5.ui.theme.F5Theme

/**
 * Table position, marked by the qualification zone it falls into.
 *
 * Colour alone would exclude a colour blind reader, so every zone also has
 * its own shape: a circle qualifies, a hexagon is a play-off, a square is
 * relegation, and a bare number means nothing in particular.
 *
 * The component knows nothing about any competition: it receives the zone as
 * data and resolves both colour and shape from its kind.
 */
@Composable
fun PositionChip(
    position: Int,
    zone: QualificationZone?,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    val colors = F5Theme.colors
    val kind = zone?.kind

    val fill: Color? = when (kind) {
        ZoneKind.QUALIFIES -> colors.zoneQualifies
        ZoneKind.PLAYOFF, ZoneKind.RELEGATION_PLAYOFF -> colors.zonePlayoff
        ZoneKind.RELEGATED -> colors.zoneRelegated
        ZoneKind.ELIMINATED, null -> null
    }

    val shape: Shape = when (kind) {
        ZoneKind.QUALIFIES -> CircleShape
        ZoneKind.PLAYOFF, ZoneKind.RELEGATION_PLAYOFF -> HexagonShape
        else -> RoundedCornerShape(7.dp)
    }

    Box(
        modifier = modifier
            .size(size)
            .then(if (fill != null) Modifier.background(fill, shape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = position.toString(),
            style = MaterialTheme.typography.labelSmall,
            // Every brand fill is light, so text on top of one is always ink.
            color = if (fill != null) colors.onBrand else colors.textTertiary
        )
    }
}

/** Flat topped hexagon, the mark used for play-off places. */
private val HexagonShape = androidx.compose.foundation.shape.GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.25f, h * 0.03f)
    lineTo(w * 0.75f, h * 0.03f)
    lineTo(w, h * 0.5f)
    lineTo(w * 0.75f, h * 0.97f)
    lineTo(w * 0.25f, h * 0.97f)
    lineTo(0f, h * 0.5f)
    close()
}

@Preview(name = "Claro", showBackground = true)
@Preview(name = "Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PositionChipPreview() {
    F5Theme {
        Surface {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                PositionChip(1, QualificationZone(1, 8, "Clasifica", ZoneKind.QUALIFIES))
                PositionChip(9, QualificationZone(9, 10, "Repechaje", ZoneKind.PLAYOFF))
                PositionChip(12, null)
                PositionChip(15, QualificationZone(15, 16, "Descenso", ZoneKind.RELEGATED))
            }
        }
    }
}
