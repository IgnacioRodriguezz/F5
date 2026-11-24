package com.rodriguez.f5.data.remote.dto

import com.google.gson.annotations.SerializedName

/*
 * Gson models of the remote payloads.
 *
 * Everything is nullable on purpose: the source is an undocumented API and a
 * missing field must degrade into an empty list, never into a crash. These
 * types never leave the data.remote package; mappers translate them into the
 * domain models before anything else sees them.
 */

// ---- /teams ----

data class TeamsResponse(@SerializedName("sports") val sports: List<SportDto>?)

data class SportDto(@SerializedName("leagues") val leagues: List<LeagueDto>?)

data class LeagueDto(
    @SerializedName("name") val name: String?,
    @SerializedName("teams") val teams: List<TeamWrapperDto>?
)

data class TeamWrapperDto(@SerializedName("team") val team: TeamDto?)

data class TeamDto(
    @SerializedName("id") val id: String?,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("shortDisplayName") val shortDisplayName: String?,
    @SerializedName("abbreviation") val abbreviation: String?,
    @SerializedName("logos") val logos: List<LogoDto>?,
    // The scoreboard returns a single "logo" string while the teams endpoint
    // returns a "logos" array. Both are read so crests load on every screen.
    @SerializedName("logo") val logo: String?
)

data class LogoDto(@SerializedName("href") val href: String?)

// ---- /standings ----

data class StandingsResponse(
    @SerializedName("name") val name: String?,
    @SerializedName("children") val children: List<StandingsGroupDto>?
)

data class StandingsGroupDto(
    @SerializedName("name") val name: String?,
    @SerializedName("abbreviation") val abbreviation: String?,
    @SerializedName("standings") val standings: StandingsTableDto?
)

data class StandingsTableDto(@SerializedName("entries") val entries: List<StandingsEntryDto>?)

data class StandingsEntryDto(
    @SerializedName("team") val team: TeamDto?,
    @SerializedName("stats") val stats: List<StatDto>?
)

data class StatDto(
    @SerializedName("name") val name: String?,
    @SerializedName("value") val value: Double?,
    @SerializedName("displayValue") val displayValue: String?
)

// ---- /scoreboard ----

data class ScoreboardResponse(@SerializedName("events") val events: List<EventDto>?)

data class EventDto(
    @SerializedName("id") val id: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("status") val status: StatusDto?,
    @SerializedName("competitions") val competitions: List<CompetitionEntryDto>?
)

data class StatusDto(
    @SerializedName("clock") val clock: Double?,
    @SerializedName("displayClock") val displayClock: String?,
    @SerializedName("type") val type: StatusTypeDto?
)

data class StatusTypeDto(
    @SerializedName("state") val state: String?,
    @SerializedName("completed") val completed: Boolean?,
    @SerializedName("description") val description: String?
)

data class CompetitionEntryDto(
    @SerializedName("competitors") val competitors: List<CompetitorDto>?
)

data class CompetitorDto(
    @SerializedName("homeAway") val homeAway: String?,
    @SerializedName("score") val score: String?,
    @SerializedName("team") val team: TeamDto?
)

// ---- /teams/{id}?enable=roster ----

data class TeamDetailResponse(@SerializedName("team") val team: TeamDetailDto?)

data class TeamDetailDto(
    @SerializedName("id") val id: String?,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("shortDisplayName") val shortDisplayName: String?,
    @SerializedName("logos") val logos: List<LogoDto>?,
    @SerializedName("athletes") val athletes: List<AthleteDto>?
)

data class AthleteDto(
    @SerializedName("id") val id: String?,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("jersey") val jersey: String?,
    @SerializedName("age") val age: Int?,
    @SerializedName("position") val position: PositionDto?,
    @SerializedName("citizenship") val citizenship: String?,
    @SerializedName("headshot") val headshot: HeadshotDto?
)

data class PositionDto(
    @SerializedName("abbreviation") val abbreviation: String?,
    @SerializedName("displayName") val displayName: String?
)

data class HeadshotDto(@SerializedName("href") val href: String?)
