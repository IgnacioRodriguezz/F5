package com.rodriguez.f5.ui.compose.team

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodriguez.f5.domain.model.Player
import com.rodriguez.f5.domain.model.PositionGroup
import com.rodriguez.f5.domain.model.Team
import com.rodriguez.f5.ui.common.UiState
import com.rodriguez.f5.ui.compose.components.CrestSize
import com.rodriguez.f5.ui.compose.components.ErrorState
import com.rodriguez.f5.ui.compose.components.LoadingSkeleton
import com.rodriguez.f5.ui.compose.components.OfflineBanner
import com.rodriguez.f5.ui.compose.components.TeamCrest
import com.rodriguez.f5.ui.theme.F5Theme

@Composable
fun TeamDetailRoute(
    viewModel: TeamDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    TeamDetailScreen(
        state = state,
        onBack = onBack,
        onToggleFavorite = viewModel::toggleFavorite,
        onRetry = viewModel::retry,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TeamDetailScreen(
    state: UiState<TeamDetailUi>,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val success = state as? UiState.Success

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = success?.data?.team?.name ?: "Equipo",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (success != null) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (success.data.isFavorite) {
                                    Icons.Default.Star
                                } else {
                                    Icons.Outlined.StarBorder
                                },
                                contentDescription = "Favorito",
                                tint = if (success.data.isFavorite) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (state) {
                is UiState.Loading -> LoadingSkeleton()
                is UiState.Empty -> ErrorState(state.message, onRetry)
                is UiState.Error -> ErrorState(state.message, onRetry)
                is UiState.Success -> TeamDetailContent(
                    ui = state.data,
                    fromCache = state.fromCache,
                    syncedAtMillis = state.syncedAtMillis
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TeamDetailContent(ui: TeamDetailUi, fromCache: Boolean, syncedAtMillis: Long) {
    Column(Modifier.fillMaxSize()) {
        if (fromCache) OfflineBanner(syncedAtMillis)

        LazyColumn(Modifier.fillMaxSize()) {
            item(key = "head") { TeamHeader(ui.team) }

            if (ui.sections.isEmpty()) {
                item(key = "nosquad") {
                    Text(
                        text = "No hay plantel publicado para este equipo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            ui.sections.forEach { section ->
                stickyHeader(key = "h-${section.group.name}") {
                    Text(
                        text = section.group.label,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
                items(section.players, key = { it.id }) { PlayerRow(it) }
            }
        }
    }
}

@Composable
private fun TeamHeader(team: Team) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamCrest(url = team.crestUrl, size = CrestSize.LARGE)
        Spacer(Modifier.width(16.dp))
        Text(
            text = team.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Squad row.
 *
 * The data source has no photographs for players of the Argentine leagues,
 * so the shirt number carries the visual weight instead of a portrait.
 */
@Composable
private fun PlayerRow(player: Player) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(F5Theme.colors.surfaceAlternate, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = player.shirtNumber?.toString() ?: "–",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val detail = listOfNotNull(
                player.position.takeIf { it.isNotBlank() },
                player.age?.let { "$it años" },
                player.nationality
            ).joinToString(" · ")
            if (detail.isNotBlank()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "Claro", showBackground = true, heightDp = 560)
@Preview(name = "Oscuro", showBackground = true, heightDp = 560, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TeamDetailPreview() {
    val team = Team("5", "Boca Juniors", "Boca Jrs.", null)
    val players = listOf(
        Player("1", "5", "Agustín Marchesín", "G", 1, 38, "Argentina", null),
        Player("2", "5", "Leandro Brey", "G", 25, 24, "Argentina", null),
        Player("3", "5", "Marcos Rojo", "D", 6, 35, "Argentina", null)
    )
    F5Theme {
        Surface {
            TeamDetailScreen(
                state = UiState.Success(
                    TeamDetailUi(
                        team = team,
                        sections = listOf(
                            SquadSection(PositionGroup.GOALKEEPER, players.take(2)),
                            SquadSection(PositionGroup.DEFENDER, players.drop(2))
                        ),
                        isFavorite = true
                    )
                ),
                onBack = {},
                onToggleFavorite = {},
                onRetry = {}
            )
        }
    }
}
