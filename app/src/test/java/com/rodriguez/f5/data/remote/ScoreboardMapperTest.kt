package com.rodriguez.f5.data.remote

import com.google.gson.Gson
import com.rodriguez.f5.data.remote.dto.ScoreboardResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Checks the fixture mapper against a scoreboard captured from the real API. */
class ScoreboardMapperTest {

    private val gson = Gson()

    private fun readScoreboard(): ScoreboardResponse {
        val json = checkNotNull(javaClass.classLoader?.getResourceAsStream("scoreboard.json")) {
            "Missing fixture scoreboard.json"
        }.bufferedReader().use { it.readText() }
        return gson.fromJson(json, ScoreboardResponse::class.java)
    }

    @Test
    fun `events are mapped into matches with both teams`() {
        val matches = EspnMapper.toMatches(readScoreboard().events, "eng.1")

        assertTrue("The fixture should contain at least one match", matches.isNotEmpty())
        matches.forEach { match ->
            assertTrue("Home team has a name", match.home.name.isNotBlank())
            assertTrue("Away team has a name", match.away.name.isNotBlank())
            assertEquals("eng.1", match.competitionSlug)
            assertTrue("Kickoff is a real instant", match.kickoffEpochMillis > 0)
            assertNotNull("Status is always resolved", match.status)
        }
    }

    @Test
    fun `home and away are told apart`() {
        val match = EspnMapper.toMatches(readScoreboard().events, "eng.1").first()
        assertTrue("Both sides are different teams", match.home.id != match.away.id)
    }

    @Test
    fun `a match without competitors is skipped instead of crashing`() {
        val broken = gson.fromJson(
            """{"events":[{"id":"1","date":"2026-11-23T20:00Z","competitions":[{"competitors":[]}]}]}""",
            ScoreboardResponse::class.java
        )
        assertEquals(emptyList<Any>(), EspnMapper.toMatches(broken.events, "eng.1"))
    }

    @Test
    fun `an empty payload yields no matches`() {
        assertEquals(emptyList<Any>(), EspnMapper.toMatches(null, "eng.1"))
    }
}
