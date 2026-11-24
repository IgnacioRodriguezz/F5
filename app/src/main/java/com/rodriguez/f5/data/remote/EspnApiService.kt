package com.rodriguez.f5.data.remote

import com.rodriguez.f5.data.remote.dto.ScoreboardResponse
import com.rodriguez.f5.data.remote.dto.StandingsResponse
import com.rodriguez.f5.data.remote.dto.TeamDetailResponse
import com.rodriguez.f5.data.remote.dto.TeamsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Remote contract.
 *
 * Every competition is read through these four endpoints: only the slug
 * changes, which is why adding a tournament costs no code.
 */
interface EspnApiService {

    @GET("apis/site/v2/sports/soccer/{slug}/teams")
    suspend fun getTeams(@Path("slug") slug: String): TeamsResponse

    @GET("apis/v2/sports/soccer/{slug}/standings")
    suspend fun getStandings(@Path("slug") slug: String): StandingsResponse

    /** [date] is formatted as yyyyMMdd; omitting it returns the current day. */
    @GET("apis/site/v2/sports/soccer/{slug}/scoreboard")
    suspend fun getScoreboard(
        @Path("slug") slug: String,
        @Query("dates") date: String? = null
    ): ScoreboardResponse

    @GET("apis/site/v2/sports/soccer/{slug}/teams/{teamId}")
    suspend fun getTeamDetail(
        @Path("slug") slug: String,
        @Path("teamId") teamId: String,
        @Query("enable") enable: String = "roster"
    ): TeamDetailResponse

    companion object {
        const val BASE_URL = "https://site.api.espn.com/"
    }
}
