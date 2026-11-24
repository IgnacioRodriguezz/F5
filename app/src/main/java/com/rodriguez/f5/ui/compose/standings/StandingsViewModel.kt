package com.rodriguez.f5.ui.compose.standings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rodriguez.f5.data.repository.FootballRepository
import com.rodriguez.f5.data.repository.RepositoryResult
import com.rodriguez.f5.domain.catalog.CompetitionCatalog
import com.rodriguez.f5.domain.model.Competition
import com.rodriguez.f5.domain.model.StandingGroup
import com.rodriguez.f5.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/** Everything the standings screen needs to draw itself. */
data class StandingsUi(
    val competition: Competition,
    val groups: List<StandingGroup>
)

class StandingsViewModel(
    private val repository: FootballRepository
) : ViewModel() {

    private val _competition = MutableStateFlow(CompetitionCatalog.default)
    val competition: StateFlow<Competition> = _competition.asStateFlow()

    private val _state = MutableStateFlow<UiState<StandingsUi>>(UiState.Loading)
    val state: StateFlow<UiState<StandingsUi>> = _state.asStateFlow()

    init {
        load()
    }

    fun selectCompetition(competition: Competition) {
        if (competition.slug == _competition.value.slug) return
        _competition.value = competition
        load()
    }

    fun retry() = load(forceRefresh = true)

    fun refresh() = load(forceRefresh = true)

    private fun load(forceRefresh: Boolean = false) {
        val current = _competition.value
        _state.value = UiState.Loading

        viewModelScope.launch {
            if (!current.hasStandings) {
                _state.value = UiState.Empty(
                    "${current.name} se juega por eliminación directa, así que no tiene tabla de posiciones."
                )
                return@launch
            }

            when (val result = repository.getStandings(current.slug, forceRefresh)) {
                is RepositoryResult.Fresh -> _state.value = toState(current, result.data, false, 0L)
                is RepositoryResult.Cached -> _state.value =
                    toState(current, result.data, true, result.syncedAtMillis)
                is RepositoryResult.Failed -> _state.value = UiState.Error(messageFor(result.cause))
            }
        }
    }

    private fun toState(
        competition: Competition,
        groups: List<StandingGroup>,
        fromCache: Boolean,
        syncedAt: Long
    ): UiState<StandingsUi> =
        if (groups.isEmpty()) {
            UiState.Empty("Todavía no hay posiciones publicadas para ${competition.name}.")
        } else {
            UiState.Success(StandingsUi(competition, groups), fromCache, syncedAt)
        }

    /** Technical failures are turned into something a person can act on. */
    private fun messageFor(cause: Throwable): String = when (cause) {
        is IOException -> "No se pudo conectar. Revisá tu conexión e intentá de nuevo."
        else -> "No se pudieron cargar las posiciones."
    }

    /**
     * Supplies the repository to the ViewModel without a dependency injection
     * framework, which is the Factory the platform expects.
     */
    class Factory(private val repository: FootballRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(StandingsViewModel::class.java)) {
                "Unknown ViewModel: ${modelClass.name}"
            }
            return StandingsViewModel(repository) as T
        }
    }
}
