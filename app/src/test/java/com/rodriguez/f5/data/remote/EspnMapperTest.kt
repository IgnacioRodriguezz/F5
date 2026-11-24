package com.rodriguez.f5.data.remote

import com.google.gson.Gson
import com.rodriguez.f5.data.remote.dto.StandingsResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Runs the mapper against responses captured from the real API.
 *
 * The source is undocumented, so these fixtures are the contract: if the
 * remote shape ever changes, these tests are what notices.
 */
class EspnMapperTest {

    private val gson = Gson()

    private fun readStandings(fileName: String): StandingsResponse {
        val json = checkNotNull(javaClass.classLoader?.getResourceAsStream(fileName)) {
            "Missing fixture $fileName"
        }.bufferedReader().use { it.readText() }
        return gson.fromJson(json, StandingsResponse::class.java)
    }

    @Test
    fun `argentine league is mapped into two zones of fifteen teams`() {
        val groups = EspnMapper.toStandingGroups(readStandings("standings_arg1.json"))

        assertEquals("Argentina should produce two zones", 2, groups.size)
        groups.forEach { assertEquals(15, it.rows.size) }
        assertTrue("Group names are translated", groups.all { it.name.startsWith("Grupo ") })
    }

    @Test
    fun `a single table competition produces one unnamed group`() {
        val groups = EspnMapper.toStandingGroups(readStandings("standings_eng1.json"))

        assertEquals(1, groups.size)
        // One table needs no header, so the group carries no name.
        assertEquals("", groups.first().name)
        assertEquals(20, groups.first().rows.size)
    }

    @Test
    fun `libertadores is mapped into eight groups of four`() {
        val groups = EspnMapper.toStandingGroups(readStandings("standings_libertadores.json"))

        assertEquals(8, groups.size)
        groups.forEach { assertEquals(4, it.rows.size) }
    }

    @Test
    fun `rows carry every statistic the table shows`() {
        val row = EspnMapper.toStandingGroups(readStandings("standings_arg1.json"))
            .first().rows.first()

        assertEquals("First row is position one", 1, row.position)
        assertTrue("Played matches are read", row.played > 0)
        assertTrue("Points are read", row.points > 0)
        assertEquals(
            "Goal difference is derived, not taken from the payload",
            row.goalsFor - row.goalsAgainst,
            row.goalDifference
        )
        assertNotNull("Teams come with a crest", row.team.crestUrl)
        assertTrue("Team names are not empty", row.team.name.isNotBlank())
    }

    @Test
    fun `rows come out sorted by position`() {
        EspnMapper.toStandingGroups(readStandings("standings_libertadores.json")).forEach { group ->
            val positions = group.rows.map { it.position }
            assertEquals(positions.sorted(), positions)
        }
    }

    @Test
    fun `a malformed payload yields no groups instead of throwing`() {
        val empty = gson.fromJson("{}", StandingsResponse::class.java)
        assertEquals(emptyList<Any>(), EspnMapper.toStandingGroups(empty))
    }
}
