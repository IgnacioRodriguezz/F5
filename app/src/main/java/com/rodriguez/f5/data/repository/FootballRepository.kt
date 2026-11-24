package com.rodriguez.f5.data.repository

import com.rodriguez.f5.data.local.dao.FavoriteDao
import com.rodriguez.f5.data.local.dao.MatchDao
import com.rodriguez.f5.data.local.dao.PlayerDao
import com.rodriguez.f5.data.local.dao.StandingDao
import com.rodriguez.f5.data.local.dao.SyncMetaDao
import com.rodriguez.f5.data.remote.EspnApiService
import com.rodriguez.f5.data.remote.EspnMapper
import com.rodriguez.f5.domain.catalog.CompetitionCatalog
import com.rodriguez.f5.domain.model.Match
import com.rodriguez.f5.domain.model.Player
import com.rodriguez.f5.domain.model.Team
import com.rodriguez.f5.domain.model.StandingGroup
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

/**
 * Result of a repository read, carrying where the data came from so the
 * screen can tell the user it is looking at saved information.
 */
sealed interface RepositoryResult<out T> {
    data class Fresh<T>(val data: T) : RepositoryResult<T>
    data class Cached<T>(val data: T, val syncedAtMillis: Long) : RepositoryResult<T>
    data class Failed(val cause: Throwable) : RepositoryResult<Nothing>
}

/**
 * Single source of truth for the UI.
 *
 * The rule is always the same: read the local database first, and only go to
 * the network when there is nothing stored or what is stored has expired. If
 * the network then fails but something is cached, the cached copy wins over
 * an error screen.
 */
class FootballRepository(
    private val api: EspnApiService,
    private val standingDao: StandingDao,
    private val matchDao: MatchDao,
    private val playerDao: PlayerDao,
    private val favoriteDao: FavoriteDao,
    private val syncMetaDao: SyncMetaDao,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getStandings(
        competitionSlug: String,
        forceRefresh: Boolean = false,
        policy: CachePolicy = CachePolicy.STANDINGS
    ): RepositoryResult<List<StandingGroup>> = withContext(io) {

        val key = "standings:$competitionSlug"
        val cached = standingDao.getForCompetition(competitionSlug)
        val expired = syncMetaDao.isExpired(key, policy.maxAgeMillis)

        if (cached.isNotEmpty() && !expired && !forceRefresh) {
            return@withContext RepositoryResult.Cached(cached, syncMetaDao.lastUpdated(key) ?: 0L)
        }

        try {
            val groups = EspnMapper.toStandingGroups(api.getStandings(competitionSlug))
            if (groups.isEmpty()) {
                // The competition exists but has no table, such as a knockout cup.
                return@withContext RepositoryResult.Fresh(emptyList())
            }
            standingDao.replaceForCompetition(competitionSlug, groups)
            syncMetaDao.markUpdated(key)
            RepositoryResult.Fresh(groups)
        } catch (error: Exception) {
            // Stale data beats an error screen when there is something to show.
            if (cached.isNotEmpty()) {
                RepositoryResult.Cached(cached, syncMetaDao.lastUpdated(key) ?: 0L)
            } else {
                RepositoryResult.Failed(error)
            }
        }
    }

    /**
     * Every match of a given day, across all competitions.
     *
     * The remote API answers one competition at a time, so the fifteen calls
     * are issued in parallel and a competition that fails is skipped rather
     * than failing the whole screen.
     */
    suspend fun getMatchesForDay(
        dayStartMillis: Long,
        forceRefresh: Boolean = false
    ): RepositoryResult<List<Match>> = withContext(io) {

        val dayEnd = dayStartMillis + DAY_MILLIS
        val key = "matches:" + dayKey(dayStartMillis)
        val cached = matchDao.getBetween(dayStartMillis, dayEnd)

        // A day with live matches goes stale in two minutes; a finished one
        // barely changes, so it is kept much longer.
        val policy = if (cached.any { it.isLive }) CachePolicy.LIVE_MATCHES else CachePolicy.MATCHES

        if (cached.isNotEmpty() && !syncMetaDao.isExpired(key, policy.maxAgeMillis) && !forceRefresh) {
            return@withContext RepositoryResult.Cached(cached, syncMetaDao.lastUpdated(key) ?: 0L)
        }

        val date = espnDate(dayStartMillis)
        val downloaded = coroutineScope {
            CompetitionCatalog.all.map { competition ->
                async {
                    runCatching {
                        val response = api.getScoreboard(competition.slug, date)
                        EspnMapper.toMatches(response.events, competition.slug)
                    }.getOrDefault(emptyList())
                }
            }.awaitAll()
        }

        val all = downloaded.flatten()
        // Every call failing means the network is down, not an empty day.
        if (all.isEmpty() && downloaded.all { it.isEmpty() } && cached.isNotEmpty()) {
            return@withContext RepositoryResult.Cached(cached, syncMetaDao.lastUpdated(key) ?: 0L)
        }

        all.groupBy { it.competitionSlug }.forEach { (slug, matches) ->
            matchDao.replaceForDay(slug, dayStartMillis, dayEnd, matches)
        }
        syncMetaDao.markUpdated(key)
        RepositoryResult.Fresh(matchDao.getBetween(dayStartMillis, dayEnd))
    }

    /** yyyyMMdd, which is the format the remote scoreboard expects. */
    private fun espnDate(millis: Long): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = millis }
        return String.format(
            Locale.US, "%04d%02d%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun dayKey(millis: Long): String = espnDate(millis)

    /**
     * A team with its squad.
     *
     * Squads barely change, so this is the most cacheable call in the app and
     * the one that works best with no network at all.
     */
    suspend fun getSquad(
        competitionSlug: String,
        teamId: String,
        forceRefresh: Boolean = false
    ): RepositoryResult<List<Player>> = withContext(io) {

        val key = "squad:" + teamId
        val cached = playerDao.getForTeam(teamId)

        if (cached.isNotEmpty() && !syncMetaDao.isExpired(key, CachePolicy.SQUAD.maxAgeMillis) && !forceRefresh) {
            return@withContext RepositoryResult.Cached(cached, syncMetaDao.lastUpdated(key) ?: 0L)
        }

        try {
            val response = api.getTeamDetail(competitionSlug, teamId)
            val players = EspnMapper.toPlayers(response.team?.athletes, teamId)
            if (players.isEmpty() && cached.isNotEmpty()) {
                return@withContext RepositoryResult.Cached(cached, syncMetaDao.lastUpdated(key) ?: 0L)
            }
            playerDao.replaceForTeam(teamId, players)
            syncMetaDao.markUpdated(key)
            RepositoryResult.Fresh(players)
        } catch (error: Exception) {
            if (cached.isNotEmpty()) {
                RepositoryResult.Cached(cached, syncMetaDao.lastUpdated(key) ?: 0L)
            } else {
                RepositoryResult.Failed(error)
            }
        }
    }

    suspend fun isFavorite(teamId: String): Boolean = withContext(io) { favoriteDao.isFavorite(teamId) }

    suspend fun toggleFavorite(teamId: String): Boolean = withContext(io) { favoriteDao.toggle(teamId) }

    suspend fun getFavoriteTeams(): List<Team> = withContext(io) { favoriteDao.getFavoriteTeams() }

    companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000

        /** Midnight of the day the given instant falls in, in local time. */
        fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
