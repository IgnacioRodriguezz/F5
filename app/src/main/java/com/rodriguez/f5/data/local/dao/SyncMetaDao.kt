package com.rodriguez.f5.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.rodriguez.f5.data.local.FootballContract.SyncMeta
import com.rodriguez.f5.data.local.FootballDatabaseHelper

/**
 * Remembers when each piece of cached data was last downloaded, which is what
 * lets the repository decide between reading locally and hitting the network.
 */
class SyncMetaDao(private val helper: FootballDatabaseHelper) {

    fun lastUpdated(key: String): Long? {
        helper.readableDatabase.rawQuery(
            "SELECT ${SyncMeta.LAST_UPDATED} FROM ${SyncMeta.TABLE} WHERE ${SyncMeta.KEY} = ?",
            arrayOf(key)
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getLong(0) else null
        }
    }

    fun markUpdated(key: String, timestamp: Long = System.currentTimeMillis()) {
        helper.writableDatabase.insertWithOnConflict(
            SyncMeta.TABLE,
            null,
            ContentValues().apply {
                put(SyncMeta.KEY, key)
                put(SyncMeta.LAST_UPDATED, timestamp)
            },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun isExpired(key: String, maxAgeMillis: Long): Boolean {
        val last = lastUpdated(key) ?: return true
        return System.currentTimeMillis() - last > maxAgeMillis
    }
}
