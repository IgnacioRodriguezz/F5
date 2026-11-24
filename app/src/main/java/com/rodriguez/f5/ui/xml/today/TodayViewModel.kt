package com.rodriguez.f5.ui.xml.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rodriguez.f5.data.repository.FootballRepository
import com.rodriguez.f5.data.repository.RepositoryResult
import com.rodriguez.f5.domain.catalog.CompetitionCatalog
import com.rodriguez.f5.domain.model.Match
import com.rodriguez.f5.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class TodayViewModel(
    private val repository: FootballRepository
) : ViewModel() {

    /** Yesterday, today and the next three days, which is what the strip shows. */
    val days: List<Long> = (-1..3).map { offset ->
        FootballRepository.startOfDay(
            System.currentTimeMillis() + offset * FootballRepository.DAY_MILLIS
        )
    }

    private val _selectedDay = MutableStateFlow(FootballRepository.startOfDay(System.currentTimeMillis()))
    val selectedDay: StateFlow<Long> = _selectedDay.asStateFlow()

    private val _state = MutableStateFlow<UiState<List<TodayItem>>>(UiState.Loading)
    val state: StateFlow<UiState<List<TodayItem>>> = _state.asStateFlow()

    init {
        load()
    }

    fun selectDay(dayStartMillis: Long) {
        if (dayStartMillis == _selectedDay.value) return
        _selectedDay.value = dayStartMillis
        load()
    }

    fun retry() = load(forceRefresh = true)

    private fun load(forceRefresh: Boolean = false) {
        val day = _selectedDay.value
        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val result = repository.getMatchesForDay(day, forceRefresh)) {
                is RepositoryResult.Fresh -> _state.value = toState(result.data, false, 0L)
                is RepositoryResult.Cached -> _state.value =
                    toState(result.data, true, result.syncedAtMillis)
                is RepositoryResult.Failed -> _state.value = UiState.Error(messageFor(result.cause))
            }
        }
    }

    private fun toState(
        matches: List<Match>,
        fromCache: Boolean,
        syncedAt: Long
    ): UiState<List<TodayItem>> {
        if (matches.isEmpty()) {
            return UiState.Empty("No hay partidos este día en las competiciones que sigue la app.")
        }
        return UiState.Success(buildItems(matches), fromCache, syncedAt)
    }

    /**
     * Flattens the matches into the sequence the adapter expects.
     *
     * The live match that started earliest is lifted out of its group and
     * shown as a hero at the top, because it is what a person opens the app
     * to see. It stays in its competition block too, so the list reads whole.
     */
    private fun buildItems(matches: List<Match>): List<TodayItem> {
        val items = mutableListOf<TodayItem>()

        matches.filter { it.isLive }
            .minByOrNull { it.kickoffEpochMillis }
            ?.let { items += TodayItem.Hero(it) }

        val byCompetition = matches.groupBy { it.competitionSlug }
        CompetitionCatalog.all.forEach { competition ->
            val ofCompetition = byCompetition[competition.slug].orEmpty()
            if (ofCompetition.isEmpty()) return@forEach

            items += TodayItem.Header(competition.slug, competition.name, ofCompetition.size)
            ofCompetition
                .sortedWith(compareByDescending<Match> { it.isLive }.thenBy { it.kickoffEpochMillis })
                .forEach { items += TodayItem.MatchRow(it) }
        }
        return items
    }

    private fun messageFor(cause: Throwable): String = when (cause) {
        is IOException -> "No se pudo conectar. Revisá tu conexión e intentá de nuevo."
        else -> "No se pudieron cargar los partidos."
    }

    class Factory(private val repository: FootballRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TodayViewModel::class.java)) {
                "Unknown ViewModel: ${modelClass.name}"
            }
            return TodayViewModel(repository) as T
        }
    }
}
