package com.rodriguez.f5.domain.model

/** One row of a league table. */
data class StandingRow(
    val position: Int,
    val team: Team,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val points: Int
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst
}

/**
 * A single table inside a competition.
 *
 * Standings are always modelled as a list of groups, never as a flat list of
 * rows: European leagues return one group, Argentine tournaments return two
 * zones and the Libertadores returns eight. One shape covers all of them.
 */
data class StandingGroup(
    val name: String,
    val rows: List<StandingRow>
)
