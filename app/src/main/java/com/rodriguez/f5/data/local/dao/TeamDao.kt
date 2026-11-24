package com.rodriguez.f5.data.local.dao

import com.rodriguez.f5.data.local.FootballContract.Team
import com.rodriguez.f5.data.local.FootballDatabaseHelper
import com.rodriguez.f5.domain.model.Team as DomainTeam

/** Reads teams already cached, which is what the search screen looks through. */
class TeamDao(private val helper: FootballDatabaseHelper) {

    fun search(query: String, limit: Int = 50): List<DomainTeam> {
        if (query.isBlank()) return emptyList()
        val result = mutableListOf<DomainTeam>()
        helper.readableDatabase.rawQuery(
            "SELECT ${Team.ID}, ${Team.NAME}, ${Team.SHORT_NAME}, ${Team.CREST_URL} FROM ${Team.TABLE} " +
                "WHERE ${Team.NAME} LIKE ? ORDER BY ${Team.NAME} ASC LIMIT ?",
            arrayOf("%$query%", limit.toString())
        ).use { c ->
            while (c.moveToNext()) {
                result += DomainTeam(
                    c.getString(0), c.getString(1), c.getString(2),
                    if (c.isNull(3)) null else c.getString(3)
                )
            }
        }
        return result
    }

    fun count(): Int {
        helper.readableDatabase.rawQuery("SELECT COUNT(*) FROM ${Team.TABLE}", null).use {
            return if (it.moveToFirst()) it.getInt(0) else 0
        }
    }
}
