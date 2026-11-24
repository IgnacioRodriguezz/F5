package com.rodriguez.f5.data.remote

import com.rodriguez.f5.data.remote.dto.AthleteDto
import com.rodriguez.f5.data.remote.dto.EventDto
import com.rodriguez.f5.data.remote.dto.StandingsEntryDto
import com.rodriguez.f5.data.remote.dto.StandingsResponse
import com.rodriguez.f5.data.remote.dto.StatDto
import com.rodriguez.f5.data.remote.dto.TeamDto
import com.rodriguez.f5.data.remote.dto.TeamsResponse
import com.rodriguez.f5.domain.model.Match
import com.rodriguez.f5.domain.model.MatchStatus
import com.rodriguez.f5.domain.model.Player
import com.rodriguez.f5.domain.model.StandingGroup
import com.rodriguez.f5.domain.model.StandingRow
import com.rodriguez.f5.domain.model.Team
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Translates the remote payloads into domain models.
 *
 * This is the boundary of the data source: if the API ever changes, or is
 * replaced altogether, only this file and the DTOs are rewritten. A malformed
 * payload produces an empty result here rather than an exception upstream.
 */
object EspnMapper {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mmX", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // ---- teams ----

    fun toTeams(response: TeamsResponse): List<Team> =
        response.sports.orEmpty()
            .flatMap { it.leagues.orEmpty() }
            .flatMap { it.teams.orEmpty() }
            .mapNotNull { it.team?.toDomain() }

    private fun TeamDto.toDomain(): Team? {
        val teamId = id ?: return null
        val fullName = displayName ?: shortDisplayName ?: return null
        return Team(
            id = teamId,
            name = fullName,
            shortName = shortDisplayName ?: abbreviation ?: fullName,
            crestUrl = logos?.firstOrNull()?.href ?: logo
        )
    }

    // ---- standings ----

    fun toStandingGroups(response: StandingsResponse): List<StandingGroup> {
        val children = response.children.orEmpty()
        val single = children.size == 1
        return children.mapNotNull { child ->
            val entries = child.standings?.entries.orEmpty()
            if (entries.isEmpty()) return@mapNotNull null
            StandingGroup(
                // A competition with one table needs no header; one with several
                // uses the group name, translated from the English the API returns.
                name = if (single) "" else localizeGroupName(child.name ?: child.abbreviation),
                rows = entries.mapNotNull { it.toRow() }.sortedBy { it.position }
            )
        }
    }

    private fun localizeGroupName(raw: String?): String = when {
        raw == null -> ""
        raw.startsWith("Group ", ignoreCase = true) -> "Grupo " + raw.removePrefix("Group ").trim()
        else -> raw
    }

    private fun StandingsEntryDto.toRow(): StandingRow? {
        val domainTeam = team?.toDomain() ?: return null
        val byName = stats.orEmpty().associateBy { it.name }
        return StandingRow(
            position = byName.int("rank"),
            team = domainTeam,
            played = byName.int("gamesPlayed"),
            won = byName.int("wins"),
            drawn = byName.int("ties"),
            lost = byName.int("losses"),
            goalsFor = byName.int("pointsFor"),
            goalsAgainst = byName.int("pointsAgainst"),
            points = byName.int("points")
        )
    }

    private fun Map<String?, StatDto>.int(key: String): Int =
        this[key]?.value?.toInt() ?: this[key]?.displayValue?.toIntOrNull() ?: 0

    // ---- scoreboard ----

    fun toMatches(events: List<EventDto>?, competitionSlug: String): List<Match> =
        events.orEmpty().mapNotNull { it.toMatch(competitionSlug) }

    private fun EventDto.toMatch(competitionSlug: String): Match? {
        val matchId = id ?: return null
        val competitors = competitions?.firstOrNull()?.competitors.orEmpty()
        val homeSide = competitors.firstOrNull { it.homeAway == "home" } ?: return null
        val awaySide = competitors.firstOrNull { it.homeAway == "away" } ?: return null
        val homeTeam = homeSide.team?.toDomain() ?: return null
        val awayTeam = awaySide.team?.toDomain() ?: return null

        val state = status?.type?.state
        val matchStatus = when {
            status?.type?.completed == true -> MatchStatus.FINISHED
            state == "in" -> MatchStatus.LIVE
            state == "post" -> MatchStatus.FINISHED
            status?.type?.description?.contains("Postponed", true) == true -> MatchStatus.POSTPONED
            else -> MatchStatus.SCHEDULED
        }

        return Match(
            id = matchId,
            competitionSlug = competitionSlug,
            home = homeTeam,
            away = awayTeam,
            homeScore = homeSide.score?.toIntOrNull(),
            awayScore = awaySide.score?.toIntOrNull(),
            kickoffEpochMillis = parseDate(date),
            status = matchStatus,
            minute = status?.clock?.toInt().takeIf { matchStatus == MatchStatus.LIVE }
        )
    }

    private fun parseDate(raw: String?): Long {
        if (raw == null) return 0L
        return runCatching { isoFormat.parse(raw)?.time ?: 0L }.getOrDefault(0L)
    }

    // ---- roster ----

    fun toPlayers(athletes: List<AthleteDto>?, teamId: String): List<Player> =
        athletes.orEmpty().mapNotNull { it.toPlayer(teamId) }

    private fun AthleteDto.toPlayer(teamId: String): Player? {
        val playerId = id ?: return null
        val playerName = displayName ?: return null
        return Player(
            id = playerId,
            teamId = teamId,
            name = playerName,
            position = position?.abbreviation ?: "",
            shirtNumber = jersey?.toIntOrNull(),
            age = age,
            nationality = citizenship,
            photoUrl = headshot?.href
        )
    }
}
