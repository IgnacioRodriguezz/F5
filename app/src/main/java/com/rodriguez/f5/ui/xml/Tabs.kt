package com.rodriguez.f5.ui.xml

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodriguez.f5.domain.catalog.CompetitionCatalog
import com.rodriguez.f5.domain.model.Team
import com.rodriguez.f5.ui.compose.standings.StandingsRoute
import com.rodriguez.f5.ui.compose.standings.StandingsViewModel
import com.rodriguez.f5.ui.compose.team.TeamDetailRoute
import com.rodriguez.f5.ui.compose.team.TeamDetailViewModel
import com.rodriguez.f5.ui.compose.teamlist.FavoritesRoute
import com.rodriguez.f5.ui.compose.teamlist.FavoritesViewModel
import com.rodriguez.f5.ui.compose.teamlist.SearchRoute
import com.rodriguez.f5.ui.compose.teamlist.SearchViewModel

/**
 * The Compose destinations of the bottom navigation bar.
 *
 * Today is built with XML views and a RecyclerView and lives in its own
 * package; these three host Compose.
 */

/**
 * Shows a list and, when a team is tapped, its detail.
 *
 * Three tabs need the same behaviour, so the switch lives here once.
 */
@Composable
private fun ComposeFragment.WithTeamDetail(
    slug: String = CompetitionCatalog.default.slug,
    list: @Composable ((Team) -> Unit) -> Unit
) {
    var selected by remember { mutableStateOf<Team?>(null) }
    val team = selected

    if (team == null) {
        list { selected = it }
    } else {
        val detail: TeamDetailViewModel = viewModel(
            key = team.id,
            factory = TeamDetailViewModel.Factory(
                repository = container.footballRepository,
                competitionSlug = slug,
                team = team
            )
        )
        TeamDetailRoute(viewModel = detail, onBack = { selected = null })
    }
}

class CompetitionsFragment : ComposeFragment() {

    private val viewModel: StandingsViewModel by viewModels {
        StandingsViewModel.Factory(container.footballRepository)
    }

    @Composable
    override fun Content() {
        val competition by viewModel.competition.collectAsStateWithLifecycle()
        WithTeamDetail(slug = competition.slug) { onTeamClick ->
            StandingsRoute(viewModel = viewModel, onTeamClick = onTeamClick)
        }
    }
}

class FavoritesFragment : ComposeFragment() {

    private val viewModel: FavoritesViewModel by viewModels {
        FavoritesViewModel.Factory(container.footballRepository)
    }

    @Composable
    override fun Content() {
        // Refreshed on entry so a team starred in the detail shows up here.
        LaunchedEffect(Unit) { viewModel.refresh() }
        WithTeamDetail { onTeamClick ->
            FavoritesRoute(viewModel = viewModel, onTeamClick = onTeamClick)
        }
    }
}

class SearchFragment : ComposeFragment() {

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModel.Factory(container.teamDao)
    }

    @Composable
    override fun Content() {
        WithTeamDetail { onTeamClick ->
            SearchRoute(viewModel = viewModel, onTeamClick = onTeamClick)
        }
    }
}
