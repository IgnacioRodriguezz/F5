package com.rodriguez.f5.domain.model

enum class MatchStatus { SCHEDULED, LIVE, FINISHED, POSTPONED }

/** A fixture, played or not. */
data class Match(
    val id: String,
    val competitionSlug: String,
    val home: Team,
    val away: Team,
    val homeScore: Int?,
    val awayScore: Int?,
    val kickoffEpochMillis: Long,
    val status: MatchStatus,
    val minute: Int?
) {
    val isLive: Boolean get() = status == MatchStatus.LIVE
    val hasScore: Boolean get() = homeScore != null && awayScore != null
}
