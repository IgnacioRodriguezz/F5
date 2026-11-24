package com.rodriguez.f5.data.local.dao

import android.content.ContentValues
import com.rodriguez.f5.data.local.FootballContract.Standing
import com.rodriguez.f5.data.local.FootballContract.Team
import com.rodriguez.f5.data.local.FootballDatabaseHelper
import com.rodriguez.f5.domain.model.StandingGroup
import com.rodriguez.f5.domain.model.StandingRow
import com.rodriguez.f5.domain.model.Team as DomainTeam

/**
 * Reads and writes league tables.
 *
 * Rows are stored flat and rebuilt into groups on read, which keeps the schema
 * simple while the domain keeps its list-of-groups shape.
 */
class StandingDao(private val helper: FootballDatabaseHelper) {

    fun replaceForCompetition(competitionSlug: String, groups: List<StandingGroup>) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(Standing.TABLE, "${Standing.COMPETITION_SLUG} = ?", arrayOf(competitionSlug))

            groups.forEachIndexed { groupIndex, group ->
                group.rows.forEach { row ->
                    db.insertWithOnConflict(
                        Team.TABLE,
                        null,
                        ContentValues().apply {
                            put(Team.ID, row.team.id)
                            put(Team.NAME, row.team.name)
                            put(Team.SHORT_NAME, row.team.shortName)
                            put(Team.CREST_URL, row.team.crestUrl)
                        },
                        android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                    )

                    db.insertWithOnConflict(
                        Standing.TABLE,
                        null,
                        ContentValues().apply {
                            put(Standing.COMPETITION_SLUG, competitionSlug)
                            put(Standing.GROUP_NAME, group.name)
                            put(Standing.GROUP_ORDER, groupIndex)
                            put(Standing.POSITION, row.position)
                            put(Standing.TEAM_ID, row.team.id)
                            put(Standing.PLAYED, row.played)
                            put(Standing.WON, row.won)
                            put(Standing.DRAWN, row.drawn)
                            put(Standing.LOST, row.lost)
                            put(Standing.GOALS_FOR, row.goalsFor)
                            put(Standing.GOALS_AGAINST, row.goalsAgainst)
                            put(Standing.POINTS, row.points)
                        },
                        android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                    )
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getForCompetition(competitionSlug: String): List<StandingGroup> {
        val sql = """
            SELECT s.${Standing.GROUP_NAME}, s.${Standing.GROUP_ORDER}, s.${Standing.POSITION},
                   s.${Standing.PLAYED}, s.${Standing.WON}, s.${Standing.DRAWN}, s.${Standing.LOST},
                   s.${Standing.GOALS_FOR}, s.${Standing.GOALS_AGAINST}, s.${Standing.POINTS},
                   t.${Team.ID}, t.${Team.NAME}, t.${Team.SHORT_NAME}, t.${Team.CREST_URL}
            FROM ${Standing.TABLE} s
            JOIN ${Team.TABLE} t ON t.${Team.ID} = s.${Standing.TEAM_ID}
            WHERE s.${Standing.COMPETITION_SLUG} = ?
            ORDER BY s.${Standing.GROUP_ORDER} ASC, s.${Standing.POSITION} ASC
        """.trimIndent()

        val grouped = linkedMapOf<String, MutableList<StandingRow>>()

        helper.readableDatabase.rawQuery(sql, arrayOf(competitionSlug)).use { cursor ->
            while (cursor.moveToNext()) {
                val groupName = cursor.getString(0)
                val row = StandingRow(
                    position = cursor.getInt(2),
                    team = DomainTeam(
                        id = cursor.getString(10),
                        name = cursor.getString(11),
                        shortName = cursor.getString(12),
                        crestUrl = if (cursor.isNull(13)) null else cursor.getString(13)
                    ),
                    played = cursor.getInt(3),
                    won = cursor.getInt(4),
                    drawn = cursor.getInt(5),
                    lost = cursor.getInt(6),
                    goalsFor = cursor.getInt(7),
                    goalsAgainst = cursor.getInt(8),
                    points = cursor.getInt(9)
                )
                grouped.getOrPut(groupName) { mutableListOf() } += row
            }
        }

        return grouped.map { (name, rows) -> StandingGroup(name, rows) }
    }
}
