package com.rodriguez.f5.ui.compose.teamlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.rodriguez.f5.data.local.dao.TeamDao
import com.rodriguez.f5.data.repository.FootballRepository
import com.rodriguez.f5.domain.model.Team
import com.rodriguez.f5.ui.compose.components.CrestSize
import com.rodriguez.f5.ui.compose.components.EmptyState
import com.rodriguez.f5.ui.compose.components.TeamCrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Row shared by the favourites and the search results. */
@Composable
fun TeamRow(team: Team, onClick: (Team) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick(team) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamCrest(url = team.crestUrl, size = CrestSize.MEDIUM)
        Spacer(Modifier.width(14.dp))
        Text(
            text = team.name,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------- Favourites ----------

class FavoritesViewModel(private val repository: FootballRepository) : ViewModel() {

    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    fun refresh() {
        viewModelScope.launch { _teams.value = repository.getFavoriteTeams() }
    }

    class Factory(private val repository: FootballRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FavoritesViewModel(repository) as T
    }
}

@Composable
fun FavoritesRoute(viewModel: FavoritesViewModel, onTeamClick: (Team) -> Unit) {
    val teams by viewModel.teams.collectAsStateWithLifecycle()

    if (teams.isEmpty()) {
        EmptyState(
            message = "Todavía no guardaste ningún equipo.\nAbrí un equipo desde la tabla y tocá la estrella."
        )
    } else {
        LazyColumn(Modifier.fillMaxSize()) {
            items(teams, key = { it.id }) { TeamRow(it, onTeamClick) }
        }
    }
}

// ---------- Search ----------

class SearchViewModel(private val teamDao: TeamDao) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<Team>>(emptyList())
    val results: StateFlow<List<Team>> = _results.asStateFlow()

    /** Searches what is already cached, so it answers instantly and offline. */
    fun onQueryChange(text: String) {
        _query.value = text
        viewModelScope.launch {
            _results.value = withContext(Dispatchers.IO) { teamDao.search(text) }
        }
    }

    class Factory(private val teamDao: TeamDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SearchViewModel(teamDao) as T
    }
}

@Composable
fun SearchRoute(viewModel: SearchViewModel, onTeamClick: (Team) -> Unit) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Buscar equipo") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        when {
            query.isBlank() -> EmptyState("Escribí el nombre de un equipo.")
            results.isEmpty() -> EmptyState("Sin resultados para \"$query\".")
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(results, key = { it.id }) { TeamRow(it, onTeamClick) }
            }
        }
    }
}
