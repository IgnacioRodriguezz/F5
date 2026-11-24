package com.rodriguez.f5.di

import android.content.Context
import com.rodriguez.f5.data.local.FootballDatabaseHelper
import com.rodriguez.f5.data.local.dao.FavoriteDao
import com.rodriguez.f5.data.local.dao.MatchDao
import com.rodriguez.f5.data.local.dao.PlayerDao
import com.rodriguez.f5.data.local.dao.StandingDao
import com.rodriguez.f5.data.local.dao.TeamDao
import com.rodriguez.f5.data.local.dao.SyncMetaDao
import com.rodriguez.f5.data.remote.NetworkModule
import com.rodriguez.f5.data.repository.FootballRepository

/**
 * Manual dependency container.
 *
 * No injection framework: the graph is small enough that wiring it by hand is
 * clearer, and it keeps the project free of annotation processing.
 */
class AppContainer(context: Context) {

    private val databaseHelper = FootballDatabaseHelper.getInstance(context)

    private val standingDao = StandingDao(databaseHelper)
    val teamDao = TeamDao(databaseHelper)
    private val matchDao = MatchDao(databaseHelper)
    private val playerDao = PlayerDao(databaseHelper)
    private val favoriteDao = FavoriteDao(databaseHelper)
    private val syncMetaDao = SyncMetaDao(databaseHelper)

    val footballRepository: FootballRepository by lazy {
        FootballRepository(
            api = NetworkModule.apiService,
            standingDao = standingDao,
            matchDao = matchDao,
            playerDao = playerDao,
            favoriteDao = favoriteDao,
            syncMetaDao = syncMetaDao
        )
    }
}
