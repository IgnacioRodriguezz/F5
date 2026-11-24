package com.rodriguez.f5.ui.compose.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rodriguez.f5.data.repository.FootballRepository
import com.rodriguez.f5.data.repository.RepositoryResult
import com.rodriguez.f5.domain.model.Player
import com.rodriguez.f5.domain.model.PositionGroup
import com.rodriguez.f5.domain.model.Team
import com.rodriguez.f5.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/** A squad split into the four usual lines. */
data class SquadSection(val group: PositionGroup, val players: List<Player>)

data class TeamDetailUi(
    val team: Team,
    val sections: List<SquadSection>,
    val isFavorite: Boolean
)

class TeamDetailViewModel(
    private val repository: FootballRepository,
    private val competitionSlug: String,
    private val team: Team
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<TeamDetailUi>>(UiState.Loading)
    val state: StateFlow<UiState<TeamDetailUi>> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    fun toggleFavorite() {
        viewModelScope.launch {
            val nowFavorite = repository.toggleFavorite(team.id)
            val current = _state.value
            if (current is UiState.Success) {
                _state.value = current.copy(data = current.data.copy(isFavorite = nowFavorite))
            }
        }
    }

    private fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val favorite = repository.isFavorite(team.id)
            when (val result = repository.getSquad(competitionSlug, team.id)) {
                is RepositoryResult.Fresh -> _state.value = toState(result.data, favorite, false, 0L)
                is RepositoryResult.Cached ->
                    _state.value = toState(result.data, favorite, true, result.syncedAtMillis)
                is RepositoryResult.Failed -> _state.value = UiState.Error(messageFor(result.cause))
            }
        }
    }

    private fun toState(
        players: List<Player>,
        favorite: Boolean,
        fromCache: Boolean,
        syncedAt: Long
    ): UiState<TeamDetailUi> {
        // Keeping the enum order puts goalkeepers first and the rest in the
        // order a team sheet is usually read.
        val sections = PositionGroup.entries.mapNotNull { group ->
            val ofGroup = players.filter { PositionGroup.fromAbbreviation(it.position) == group }
            if (ofGroup.isEmpty()) null else SquadSection(group, ofGroup)
        }
        return UiState.Success(TeamDetailUi(team, sections, favorite), fromCache, syncedAt)
    }

    private fun messageFor(cause: Throwable): String = when (cause) {
        is IOException -> "No se pudo conectar. Revisá tu conexión e intentá de nuevo."
        else -> "No se pudo cargar el plantel."
    }

    class Factory(
        private val repository: FootballRepository,
        private val competitionSlug: String,
        private val team: Team
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TeamDetailViewModel::class.java)) {
                "Unknown ViewModel: ${modelClass.name}"
            }
            return TeamDetailViewModel(repository, competitionSlug, team) as T
        }
    }
}
