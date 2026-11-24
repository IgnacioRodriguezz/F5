package com.rodriguez.f5.ui.compose.standings

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodriguez.f5.domain.catalog.CompetitionCatalog
import com.rodriguez.f5.domain.model.Competition
import com.rodriguez.f5.domain.model.QualificationZone
import com.rodriguez.f5.domain.model.Region
import com.rodriguez.f5.domain.model.StandingGroup
import com.rodriguez.f5.domain.model.StandingRow
import com.rodriguez.f5.domain.model.Team
import com.rodriguez.f5.domain.model.ZoneKind
import com.rodriguez.f5.ui.common.UiState
import com.rodriguez.f5.ui.compose.components.EmptyState
import com.rodriguez.f5.ui.compose.components.ErrorState
import com.rodriguez.f5.ui.compose.components.LoadingSkeleton
import com.rodriguez.f5.ui.compose.components.OfflineBanner
import com.rodriguez.f5.ui.compose.components.StandingHeaderRow
import com.rodriguez.f5.ui.compose.components.StandingRowItem
import com.rodriguez.f5.ui.theme.F5Theme

/**
 * Connects the screen to its ViewModel.
 *
 * Everything stateful lives here; the screen below is a pure function of its
 * arguments, which is what makes it previewable and testable.
 */
@Composable
fun StandingsRoute(
    viewModel: StandingsViewModel,
    onTeamClick: (Team) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val competition by viewModel.competition.collectAsStateWithLifecycle()

    StandingsScreen(
        state = state,
        selected = competition,
        onSelect = viewModel::selectCompetition,
        onTeamClick = onTeamClick,
        onRetry = viewModel::retry,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsScreen(
    state: UiState<StandingsUi>,
    selected: Competition,
    onSelect: (Competition) -> Unit,
    onTeamClick: (Team) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(selected.name) },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Elegir liga")
                    }
                    CompetitionMenu(
                        expanded = menuOpen,
                        selected = selected,
                        onDismiss = { menuOpen = false },
                        onSelect = { onSelect(it); menuOpen = false }
                    )
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
        Box(Modifier.weight(1f)) {
            when (state) {
                is UiState.Loading -> LoadingSkeleton()
                is UiState.Empty -> EmptyState(message = state.message)
                is UiState.Error -> ErrorState(message = state.message, onRetry = onRetry)
                is UiState.Success -> StandingsContent(
                    ui = state.data,
                    fromCache = state.fromCache,
                    syncedAtMillis = state.syncedAtMillis,
                    onTeamClick = onTeamClick
                )
            }
        }
        }
    }
}

/**
 * Menu with the fifteen competitions, grouped by region.
 *
 * It reads the catalog directly: adding a tournament there makes it appear
 * here with no change to this screen.
 */
@Composable
private fun CompetitionMenu(
    expanded: Boolean,
    selected: Competition,
    onDismiss: () -> Unit,
    onSelect: (Competition) -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        CompetitionCatalog.byRegion.forEach { (region, competitions) ->
            Text(
                text = region.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            competitions.forEach { competition ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = competition.name,
                            color = if (competition.slug == selected.slug) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    },
                    onClick = { onSelect(competition) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StandingsContent(
    ui: StandingsUi,
    fromCache: Boolean,
    syncedAtMillis: Long,
    onTeamClick: (Team) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        if (fromCache) OfflineBanner(syncedAtMillis)

        LazyColumn(Modifier.fillMaxSize()) {
            ui.groups.forEach { group ->
                stickyHeader(key = "header-" + group.name) {
                    GroupHeader(name = group.name)
                }
                items(group.rows, key = { group.name + "-" + it.team.id }) { row ->
                    StandingRowItem(
                        row = row,
                        zone = ui.competition.zoneFor(row.position),
                        onClick = onTeamClick,
                        alternate = row.position % 2 == 0
                    )
                }
            }
            if (ui.competition.qualificationZones.isNotEmpty()) {
                item(key = "legend") { ZoneLegend(ui.competition.qualificationZones) }
            }
        }
    }
}

@Composable
private fun GroupHeader(name: String) {
    Column(Modifier.fillMaxWidth()) {
        if (name.isNotBlank()) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
        StandingHeaderRow()
    }
}

/** Explains what each position colour means in this competition. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ZoneLegend(zones: List<QualificationZone>) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        zones.forEach { zone ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(colorForLegend(zone), CircleShape)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = zone.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun colorForLegend(zone: QualificationZone) = when (zone.kind) {
    ZoneKind.QUALIFIES -> F5Theme.colors.zoneQualifies
    ZoneKind.PLAYOFF, ZoneKind.RELEGATION_PLAYOFF -> F5Theme.colors.zonePlayoff
    ZoneKind.RELEGATED -> F5Theme.colors.zoneRelegated
    ZoneKind.ELIMINATED -> F5Theme.colors.zoneNeutral
}

// ---- previews ----

@Preview(name = "Con datos", showBackground = true, heightDp = 560)
@Preview(name = "Oscuro", showBackground = true, heightDp = 560, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StandingsSuccessPreview() {
    F5Theme {
        Surface {
            StandingsScreen(
                state = UiState.Success(previewUi()),
                selected = previewUi().competition,
                onSelect = {},
                onTeamClick = {},
                onRetry = {}
            )
        }
    }
}

@Preview(name = "Cargando", showBackground = true, heightDp = 400)
@Composable
private fun StandingsLoadingPreview() {
    F5Theme { Surface { StandingsScreen(UiState.Loading, CompetitionCatalog.default, {}, {}, {}) } }
}

@Preview(name = "Error", showBackground = true, heightDp = 400)
@Composable
private fun StandingsErrorPreview() {
    F5Theme {
        Surface {
            StandingsScreen(
                state = UiState.Error("No se pudo conectar. Revisá tu conexión e intentá de nuevo."),
                selected = CompetitionCatalog.default,
                onSelect = {},
                onTeamClick = {},
                onRetry = {}
            )
        }
    }
}

@Preview(name = "Sin tabla", showBackground = true, heightDp = 400)
@Composable
private fun StandingsEmptyPreview() {
    F5Theme {
        Surface {
            StandingsScreen(
                state = UiState.Empty("Copa Argentina se juega por eliminación directa, así que no tiene tabla."),
                selected = CompetitionCatalog.default,
                onSelect = {},
                onTeamClick = {},
                onRetry = {}
            )
        }
    }
}

private fun previewUi(): StandingsUi {
    val competition = Competition(
        slug = "arg.1",
        name = "Liga Profesional",
        region = Region.ARGENTINA,
        hasStandings = true,
        qualificationZones = listOf(QualificationZone(1, 8, "Octavos de final", ZoneKind.QUALIFIES))
    )
    val names = listOf(
        "Instituto" to 34, "Boca Jrs." to 33, "Vélez" to 28, "Defensa" to 28,
        "Lanús" to 26, "Newell's" to 23, "Independiente" to 23, "Estudiantes" to 16
    )
    val rows = names.mapIndexed { index, (name, points) ->
        StandingRow(
            position = index + 1,
            team = Team(id = name, name = name, shortName = name, crestUrl = null),
            played = 16, won = points / 3, drawn = 1, lost = 3,
            goalsFor = 26, goalsAgainst = 16, points = points
        )
    }
    return StandingsUi(competition, listOf(StandingGroup("Grupo A", rows)))
}
