package com.rodriguez.f5.ui.compose.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodriguez.f5.domain.model.QualificationZone
import com.rodriguez.f5.domain.model.StandingRow
import com.rodriguez.f5.domain.model.Team
import com.rodriguez.f5.domain.model.ZoneKind
import com.rodriguez.f5.ui.theme.F5Theme

/** Numeric cell of fixed width, so every column lines up down the table. */
@Composable
private fun StatCell(
    value: String,
    width: Int,
    emphasised: Boolean = false
) {
    Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (emphasised) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = Modifier.width(width.dp)
    )
}

/**
 * One line of a league table.
 *
 * Stateless by design: it receives the row, the zone it belongs to and a
 * click callback, and owns no state of its own. That is what makes it
 * previewable and reusable in the competition table, in favourites and in
 * the small table shown inside a team's detail.
 */
@Composable
fun StandingRowItem(
    row: StandingRow,
    zone: QualificationZone?,
    onClick: (Team) -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    alternate: Boolean = false
) {
    val background = when {
        highlighted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        alternate -> F5Theme.colors.surfaceAlternate
        else -> MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .clickable { onClick(row.team) }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .height(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PositionChip(position = row.position, zone = zone)
        Spacer(Modifier.width(10.dp))
        TeamCrest(url = row.team.crestUrl, size = CrestSize.SMALL)
        Spacer(Modifier.width(10.dp))
        Text(
            text = row.team.shortName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        StatCell(row.played.toString(), width = 28)
        StatCell(formatDifference(row.goalDifference), width = 36)
        StatCell(row.points.toString(), width = 32, emphasised = true)
    }
}

private fun formatDifference(difference: Int): String =
    if (difference > 0) "+$difference" else difference.toString()

/** Column headers, kept next to the row so both stay in step. */
@Composable
fun StandingHeaderRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Spacer(Modifier.width(22.dp + 10.dp + 24.dp + 10.dp))
        Text(
            text = "Equipo",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        HeaderCell("PJ", 28)
        HeaderCell("DG", 36)
        HeaderCell("PTS", 32)
    }
}

@Composable
private fun HeaderCell(label: String, width: Int) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(width.dp)
    )
}

@Preview(name = "Claro", showBackground = true)
@Preview(name = "Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StandingRowPreview() {
    F5Theme {
        Surface {
            androidx.compose.foundation.layout.Column {
                StandingHeaderRow()
                StandingRowItem(
                    row = previewRow(1, "Instituto", 34),
                    zone = QualificationZone(1, 8, "Octavos", ZoneKind.QUALIFIES),
                    onClick = {}
                )
                StandingRowItem(
                    row = previewRow(2, "Boca Jrs.", 33),
                    zone = QualificationZone(1, 8, "Octavos", ZoneKind.QUALIFIES),
                    onClick = {},
                    highlighted = true
                )
                StandingRowItem(
                    row = previewRow(12, "Estudiantes de La Plata", 16),
                    zone = null,
                    onClick = {},
                    alternate = true
                )
            }
        }
    }
}

private fun previewRow(position: Int, name: String, points: Int) = StandingRow(
    position = position,
    team = Team(id = name, name = name, shortName = name, crestUrl = null),
    played = 16,
    won = points / 3,
    drawn = 1,
    lost = 2,
    goalsFor = 26,
    goalsAgainst = 15,
    points = points
)
