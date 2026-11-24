package com.rodriguez.f5.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.rodriguez.f5.data.local.FootballContract.CompetitionTeam
import com.rodriguez.f5.data.local.FootballContract.Favorite
import com.rodriguez.f5.data.local.FootballContract.Match
import com.rodriguez.f5.data.local.FootballContract.Player
import com.rodriguez.f5.data.local.FootballContract.Standing
import com.rodriguez.f5.data.local.FootballContract.SyncMeta
import com.rodriguez.f5.data.local.FootballContract.Team

/**
 * Local database, written directly on SQLite as the assignment requires.
 *
 * It stores two different things: a cache of everything downloaded, so the app
 * works without network, and the user's own data, which is only the favourites
 * and must never be dropped by a cache refresh.
 */
class FootballDatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${Team.TABLE} (
                ${Team.ID} TEXT PRIMARY KEY,
                ${Team.NAME} TEXT NOT NULL,
                ${Team.SHORT_NAME} TEXT NOT NULL,
                ${Team.CREST_URL} TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE ${CompetitionTeam.TABLE} (
                ${CompetitionTeam.COMPETITION_SLUG} TEXT NOT NULL,
                ${CompetitionTeam.TEAM_ID} TEXT NOT NULL,
                PRIMARY KEY (${CompetitionTeam.COMPETITION_SLUG}, ${CompetitionTeam.TEAM_ID})
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE ${Standing.TABLE} (
                ${Standing.COMPETITION_SLUG} TEXT NOT NULL,
                ${Standing.GROUP_NAME} TEXT NOT NULL,
                ${Standing.GROUP_ORDER} INTEGER NOT NULL,
                ${Standing.POSITION} INTEGER NOT NULL,
                ${Standing.TEAM_ID} TEXT NOT NULL,
                ${Standing.PLAYED} INTEGER NOT NULL,
                ${Standing.WON} INTEGER NOT NULL,
                ${Standing.DRAWN} INTEGER NOT NULL,
                ${Standing.LOST} INTEGER NOT NULL,
                ${Standing.GOALS_FOR} INTEGER NOT NULL,
                ${Standing.GOALS_AGAINST} INTEGER NOT NULL,
                ${Standing.POINTS} INTEGER NOT NULL,
                PRIMARY KEY (${Standing.COMPETITION_SLUG}, ${Standing.GROUP_NAME}, ${Standing.TEAM_ID})
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE ${Match.TABLE} (
                ${Match.ID} TEXT PRIMARY KEY,
                ${Match.COMPETITION_SLUG} TEXT NOT NULL,
                ${Match.HOME_TEAM_ID} TEXT NOT NULL,
                ${Match.AWAY_TEAM_ID} TEXT NOT NULL,
                ${Match.HOME_SCORE} INTEGER,
                ${Match.AWAY_SCORE} INTEGER,
                ${Match.KICKOFF} INTEGER NOT NULL,
                ${Match.STATUS} TEXT NOT NULL,
                ${Match.MINUTE} INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE ${Player.TABLE} (
                ${Player.ID} TEXT PRIMARY KEY,
                ${Player.TEAM_ID} TEXT NOT NULL,
                ${Player.NAME} TEXT NOT NULL,
                ${Player.POSITION} TEXT NOT NULL,
                ${Player.SHIRT_NUMBER} INTEGER,
                ${Player.AGE} INTEGER,
                ${Player.NATIONALITY} TEXT,
                ${Player.PHOTO_URL} TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE ${Favorite.TABLE} (
                ${Favorite.ENTITY_TYPE} TEXT NOT NULL,
                ${Favorite.ENTITY_ID} TEXT NOT NULL,
                ${Favorite.ADDED_AT} INTEGER NOT NULL,
                PRIMARY KEY (${Favorite.ENTITY_TYPE}, ${Favorite.ENTITY_ID})
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE ${SyncMeta.TABLE} (
                ${SyncMeta.KEY} TEXT PRIMARY KEY,
                ${SyncMeta.LAST_UPDATED} INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX idx_standing_competition ON ${Standing.TABLE}(${Standing.COMPETITION_SLUG})")
        db.execSQL("CREATE INDEX idx_match_competition ON ${Match.TABLE}(${Match.COMPETITION_SLUG})")
        db.execSQL("CREATE INDEX idx_match_kickoff ON ${Match.TABLE}(${Match.KICKOFF})")
        db.execSQL("CREATE INDEX idx_player_team ON ${Player.TABLE}(${Player.TEAM_ID})")
    }

    /**
     * Everything except favourites is a cache that can be rebuilt from the
     * network, so upgrading drops and recreates it. Favourites are copied
     * across instead of being lost.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val savedFavorites = readFavorites(db)

        listOf(
            Team.TABLE, CompetitionTeam.TABLE, Standing.TABLE,
            Match.TABLE, Player.TABLE, Favorite.TABLE, SyncMeta.TABLE
        ).forEach { db.execSQL("DROP TABLE IF EXISTS $it") }

        onCreate(db)
        savedFavorites.forEach { (type, id, addedAt) ->
            db.execSQL(
                "INSERT OR IGNORE INTO ${Favorite.TABLE} " +
                    "(${Favorite.ENTITY_TYPE}, ${Favorite.ENTITY_ID}, ${Favorite.ADDED_AT}) VALUES (?, ?, ?)",
                arrayOf(type, id, addedAt)
            )
        }
    }

    private fun readFavorites(db: SQLiteDatabase): List<Triple<String, String, Long>> {
        val result = mutableListOf<Triple<String, String, Long>>()
        runCatching {
            db.rawQuery(
                "SELECT ${Favorite.ENTITY_TYPE}, ${Favorite.ENTITY_ID}, ${Favorite.ADDED_AT} FROM ${Favorite.TABLE}",
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    result += Triple(cursor.getString(0), cursor.getString(1), cursor.getLong(2))
                }
            }
        }
        return result
    }

    companion object {
        private const val DATABASE_NAME = "futbol.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var instance: FootballDatabaseHelper? = null

        /** A single helper per process: SQLiteOpenHelper is thread safe, several are not. */
        fun getInstance(context: Context): FootballDatabaseHelper =
            instance ?: synchronized(this) {
                instance ?: FootballDatabaseHelper(context).also { instance = it }
            }
    }
}
