package com.rodriguez.f5.data.repository

import java.util.concurrent.TimeUnit

/**
 * How long each kind of data stays fresh.
 *
 * Kept as a value rather than scattered constants so the repository can take
 * the policy as a parameter: a table refreshes often, a squad almost never,
 * and a test can inject a policy that never caches.
 */
data class CachePolicy(val maxAgeMillis: Long) {

    companion object {
        val STANDINGS = CachePolicy(TimeUnit.HOURS.toMillis(1))
        val LIVE_STANDINGS = CachePolicy(TimeUnit.MINUTES.toMillis(2))
        val MATCHES = CachePolicy(TimeUnit.MINUTES.toMillis(30))
        val LIVE_MATCHES = CachePolicy(TimeUnit.MINUTES.toMillis(2))
        val TEAMS = CachePolicy(TimeUnit.DAYS.toMillis(7))
        val SQUAD = CachePolicy(TimeUnit.DAYS.toMillis(7))
        val NEVER = CachePolicy(0L)
    }
}
