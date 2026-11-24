package com.rodriguez.f5.data.local

/**
 * Names of the tables and columns of the local database, in one place so the
 * DAOs never hardcode a string that the schema could change.
 */
object FootballContract {

    object Team {
        const val TABLE = "team"
        const val ID = "id"
        const val NAME = "name"
        const val SHORT_NAME = "short_name"
        const val CREST_URL = "crest_url"
    }

    object CompetitionTeam {
        const val TABLE = "competition_team"
        const val COMPETITION_SLUG = "competition_slug"
        const val TEAM_ID = "team_id"
    }

    object Standing {
        const val TABLE = "standing_row"
        const val COMPETITION_SLUG = "competition_slug"
        const val GROUP_NAME = "group_name"
        const val GROUP_ORDER = "group_order"
        const val POSITION = "position"
        const val TEAM_ID = "team_id"
        const val PLAYED = "played"
        const val WON = "won"
        const val DRAWN = "drawn"
        const val LOST = "lost"
        const val GOALS_FOR = "goals_for"
        const val GOALS_AGAINST = "goals_against"
        const val POINTS = "points"
    }

    object Match {
        const val TABLE = "match_row"
        const val ID = "id"
        const val COMPETITION_SLUG = "competition_slug"
        const val HOME_TEAM_ID = "home_team_id"
        const val AWAY_TEAM_ID = "away_team_id"
        const val HOME_SCORE = "home_score"
        const val AWAY_SCORE = "away_score"
        const val KICKOFF = "kickoff"
        const val STATUS = "status"
        const val MINUTE = "minute"
    }

    object Player {
        const val TABLE = "player"
        const val ID = "id"
        const val TEAM_ID = "team_id"
        const val NAME = "name"
        const val POSITION = "position"
        const val SHIRT_NUMBER = "shirt_number"
        const val AGE = "age"
        const val NATIONALITY = "nationality"
        const val PHOTO_URL = "photo_url"
    }

    object Favorite {
        const val TABLE = "favorite"
        const val ENTITY_TYPE = "entity_type"
        const val ENTITY_ID = "entity_id"
        const val ADDED_AT = "added_at"

        const val TYPE_TEAM = "team"
    }

    object SyncMeta {
        const val TABLE = "sync_meta"
        const val KEY = "key"
        const val LAST_UPDATED = "last_updated"
    }
}
