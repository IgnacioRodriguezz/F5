package com.rodriguez.f5.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.rodriguez.f5.data.local.FootballContract.Favorite
import com.rodriguez.f5.data.local.FootballContract.Team
import com.rodriguez.f5.data.local.FootballDatabaseHelper
import com.rodriguez.f5.domain.model.Team as DomainTeam

/**
 * The user's saved teams.
 *
 * This is the only table that is not a cache: it holds data the user created
 * and it survives a database upgrade.
 */
class FavoriteDao(private val helper: FootballDatabaseHelper) {

    fun isFavorite(teamId: String): Boolean {
        helper.readableDatabase.rawQuery(
            "SELECT 1 FROM ${Favorite.TABLE} WHERE ${Favorite.ENTITY_TYPE} = ? AND ${Favorite.ENTITY_ID} = ?",
            arrayOf(Favorite.TYPE_TEAM, teamId)
        ).use { return it.moveToFirst() }
    }

    /** Adds or removes the team and returns whether it is a favourite now. */
    fun toggle(teamId: String): Boolean {
        val db = helper.writableDatabase
        return if (isFavorite(teamId)) {
            db.delete(
                Favorite.TABLE,
                "${Favorite.ENTITY_TYPE} = ? AND ${Favorite.ENTITY_ID} = ?",
                arrayOf(Favorite.TYPE_TEAM, teamId)
            )
            false
        } else {
            db.insertWithOnConflict(
                Favorite.TABLE,
                null,
                ContentValues().apply {
                    put(Favorite.ENTITY_TYPE, Favorite.TYPE_TEAM)
                    put(Favorite.ENTITY_ID, teamId)
                    put(Favorite.ADDED_AT, System.currentTimeMillis())
                },
                SQLiteDatabase.CONFLICT_REPLACE
            )
            true
        }
    }

    fun getFavoriteTeams(): List<DomainTeam> {
        val sql = """
            SELECT t.${Team.ID}, t.${Team.NAME}, t.${Team.SHORT_NAME}, t.${Team.CREST_URL}
            FROM ${Favorite.TABLE} f
            JOIN ${Team.TABLE} t ON t.${Team.ID} = f.${Favorite.ENTITY_ID}
            WHERE f.${Favorite.ENTITY_TYPE} = ?
            ORDER BY f.${Favorite.ADDED_AT} ASC
        """.trimIndent()

        val result = mutableListOf<DomainTeam>()
        helper.readableDatabase.rawQuery(sql, arrayOf(Favorite.TYPE_TEAM)).use { c ->
            while (c.moveToNext()) {
                result += DomainTeam(
                    id = c.getString(0),
                    name = c.getString(1),
                    shortName = c.getString(2),
                    crestUrl = if (c.isNull(3)) null else c.getString(3)
                )
            }
        }
        return result
    }
}
