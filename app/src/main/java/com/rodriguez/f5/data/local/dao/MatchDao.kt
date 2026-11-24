package com.rodriguez.f5.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.rodriguez.f5.data.local.FootballContract.Match
import com.rodriguez.f5.data.local.FootballContract.Team
import com.rodriguez.f5.data.local.FootballDatabaseHelper
import com.rodriguez.f5.domain.model.MatchStatus
import com.rodriguez.f5.domain.model.Team as DomainTeam
import com.rodriguez.f5.domain.model.Match as DomainMatch

/** Reads and writes fixtures, joining each one with both teams. */
class MatchDao(private val helper: FootballDatabaseHelper) {

    fun replaceForDay(competitionSlug: String, dayStart: Long, dayEnd: Long, matches: List<DomainMatch>) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(
                Match.TABLE,
                "${Match.COMPETITION_SLUG} = ? AND ${Match.KICKOFF} >= ? AND ${Match.KICKOFF} < ?",
                arrayOf(competitionSlug, dayStart.toString(), dayEnd.toString())
            )

            matches.forEach { match ->
                saveTeam(db, match.home)
                saveTeam(db, match.away)
                db.insertWithOnConflict(
                    Match.TABLE,
                    null,
                    ContentValues().apply {
                        put(Match.ID, match.id)
                        put(Match.COMPETITION_SLUG, match.competitionSlug)
                        put(Match.HOME_TEAM_ID, match.home.id)
                        put(Match.AWAY_TEAM_ID, match.away.id)
                        put(Match.HOME_SCORE, match.homeScore)
                        put(Match.AWAY_SCORE, match.awayScore)
                        put(Match.KICKOFF, match.kickoffEpochMillis)
                        put(Match.STATUS, match.status.name)
                        put(Match.MINUTE, match.minute)
                    },
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun saveTeam(db: SQLiteDatabase, team: DomainTeam) {
        db.insertWithOnConflict(
            Team.TABLE,
            null,
            ContentValues().apply {
                put(Team.ID, team.id)
                put(Team.NAME, team.name)
                put(Team.SHORT_NAME, team.shortName)
                put(Team.CREST_URL, team.crestUrl)
            },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getBetween(dayStart: Long, dayEnd: Long): List<DomainMatch> {
        val sql = """
            SELECT m.${Match.ID}, m.${Match.COMPETITION_SLUG}, m.${Match.HOME_SCORE},
                   m.${Match.AWAY_SCORE}, m.${Match.KICKOFF}, m.${Match.STATUS}, m.${Match.MINUTE},
                   h.${Team.ID}, h.${Team.NAME}, h.${Team.SHORT_NAME}, h.${Team.CREST_URL},
                   a.${Team.ID}, a.${Team.NAME}, a.${Team.SHORT_NAME}, a.${Team.CREST_URL}
            FROM ${Match.TABLE} m
            JOIN ${Team.TABLE} h ON h.${Team.ID} = m.${Match.HOME_TEAM_ID}
            JOIN ${Team.TABLE} a ON a.${Team.ID} = m.${Match.AWAY_TEAM_ID}
            WHERE m.${Match.KICKOFF} >= ? AND m.${Match.KICKOFF} < ?
            ORDER BY m.${Match.KICKOFF} ASC
        """.trimIndent()

        val result = mutableListOf<DomainMatch>()
        helper.readableDatabase.rawQuery(sql, arrayOf(dayStart.toString(), dayEnd.toString())).use { c ->
            while (c.moveToNext()) {
                result += DomainMatch(
                    id = c.getString(0),
                    competitionSlug = c.getString(1),
                    homeScore = if (c.isNull(2)) null else c.getInt(2),
                    awayScore = if (c.isNull(3)) null else c.getInt(3),
                    kickoffEpochMillis = c.getLong(4),
                    status = runCatching { MatchStatus.valueOf(c.getString(5)) }
                        .getOrDefault(MatchStatus.SCHEDULED),
                    minute = if (c.isNull(6)) null else c.getInt(6),
                    home = DomainTeam(c.getString(7), c.getString(8), c.getString(9), c.getStringOrNull(10)),
                    away = DomainTeam(c.getString(11), c.getString(12), c.getString(13), c.getStringOrNull(14))
                )
            }
        }
        return result
    }

    private fun android.database.Cursor.getStringOrNull(index: Int): String? =
        if (isNull(index)) null else getString(index)
}
