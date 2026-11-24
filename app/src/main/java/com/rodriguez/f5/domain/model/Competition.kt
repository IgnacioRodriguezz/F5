package com.rodriguez.f5.domain.model

/** Geographical grouping used to split the competition list into sections. */
enum class Region(val label: String) {
    ARGENTINA("Argentina"),
    CONMEBOL("Conmebol"),
    EUROPE("Europa")
}

/**
 * A range of table positions that share a meaning, such as "qualifies" or
 * "relegated".
 *
 * The API does not expose this information: it is editorial configuration
 * declared once per competition in [com.rodriguez.f5.domain.catalog.CompetitionCatalog].
 * Keeping it as data is what allows a single table component to render every
 * competition without branching on its name.
 */
data class QualificationZone(
    val fromPosition: Int,
    val toPosition: Int,
    val label: String,
    val kind: ZoneKind
) {
    fun contains(position: Int): Boolean = position in fromPosition..toPosition
}

enum class ZoneKind { QUALIFIES, PLAYOFF, RELEGATION_PLAYOFF, RELEGATED, ELIMINATED }

/**
 * A tournament the app can show.
 *
 * [slug] is the identifier used by the remote API; every competition is read
 * through the same endpoints, so adding one is adding an entry to the catalog.
 */
data class Competition(
    val slug: String,
    val name: String,
    val region: Region,
    val hasStandings: Boolean,
    val qualificationZones: List<QualificationZone> = emptyList()
) {
    fun zoneFor(position: Int): QualificationZone? =
        qualificationZones.firstOrNull { it.contains(position) }
}
