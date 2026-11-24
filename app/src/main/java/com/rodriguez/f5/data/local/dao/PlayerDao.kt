package com.rodriguez.f5.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.rodriguez.f5.data.local.FootballContract.Player
import com.rodriguez.f5.data.local.FootballDatabaseHelper
import com.rodriguez.f5.domain.model.Player as DomainPlayer

/** Reads and writes squads. */
class PlayerDao(private val helper: FootballDatabaseHelper) {

    fun replaceForTeam(teamId: String, players: List<DomainPlayer>) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(Player.TABLE, "${Player.TEAM_ID} = ?", arrayOf(teamId))
            players.forEach { player ->
                db.insertWithOnConflict(
                    Player.TABLE,
                    null,
                    ContentValues().apply {
                        put(Player.ID, player.id)
                        put(Player.TEAM_ID, player.teamId)
                        put(Player.NAME, player.name)
                        put(Player.POSITION, player.position)
                        put(Player.SHIRT_NUMBER, player.shirtNumber)
                        put(Player.AGE, player.age)
                        put(Player.NATIONALITY, player.nationality)
                        put(Player.PHOTO_URL, player.photoUrl)
                    },
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getForTeam(teamId: String): List<DomainPlayer> {
        val result = mutableListOf<DomainPlayer>()
        helper.readableDatabase.rawQuery(
            "SELECT ${Player.ID}, ${Player.NAME}, ${Player.POSITION}, ${Player.SHIRT_NUMBER}, " +
                "${Player.AGE}, ${Player.NATIONALITY}, ${Player.PHOTO_URL} " +
                "FROM ${Player.TABLE} WHERE ${Player.TEAM_ID} = ? ORDER BY ${Player.SHIRT_NUMBER} IS NULL, " +
                "${Player.SHIRT_NUMBER} ASC",
            arrayOf(teamId)
        ).use { c ->
            while (c.moveToNext()) {
                result += DomainPlayer(
                    id = c.getString(0),
                    teamId = teamId,
                    name = c.getString(1),
                    position = c.getString(2),
                    shirtNumber = if (c.isNull(3)) null else c.getInt(3),
                    age = if (c.isNull(4)) null else c.getInt(4),
                    nationality = if (c.isNull(5)) null else c.getString(5),
                    photoUrl = if (c.isNull(6)) null else c.getString(6)
                )
            }
        }
        return result
    }
}
