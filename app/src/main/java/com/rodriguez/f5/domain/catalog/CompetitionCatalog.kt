package com.rodriguez.f5.domain.catalog

import com.rodriguez.f5.domain.model.Competition
import com.rodriguez.f5.domain.model.QualificationZone
import com.rodriguez.f5.domain.model.Region
import com.rodriguez.f5.domain.model.ZoneKind

/**
 * The competitions the app offers.
 *
 * Every one of them is read through the same endpoints with a different slug,
 * so this list is the only place that has to change to add or remove a
 * tournament. No screen, model or query knows which competitions exist.
 *
 * Qualification zones are editorial data, not something the API returns, and
 * they change between seasons: review them before each delivery.
 */
object CompetitionCatalog {

    private fun qualifies(from: Int, to: Int, label: String) =
        QualificationZone(from, to, label, ZoneKind.QUALIFIES)

    private fun playoff(from: Int, to: Int, label: String) =
        QualificationZone(from, to, label, ZoneKind.PLAYOFF)

    private fun relegated(from: Int, to: Int, label: String = "Descenso") =
        QualificationZone(from, to, label, ZoneKind.RELEGATED)

    val all: List<Competition> = listOf(

        // ---- Argentina ----
        Competition(
            slug = "arg.1",
            name = "Liga Profesional",
            region = Region.ARGENTINA,
            hasStandings = true,
            qualificationZones = listOf(qualifies(1, 8, "Octavos de final"))
        ),
        Competition(
            slug = "arg.copa_lpf",
            name = "Copa de la Liga Profesional",
            region = Region.ARGENTINA,
            hasStandings = false
        ),
        Competition(
            slug = "arg.2",
            name = "Primera Nacional",
            region = Region.ARGENTINA,
            hasStandings = true,
            qualificationZones = listOf(
                qualifies(1, 1, "Ascenso directo"),
                playoff(2, 8, "Reducido")
            )
        ),
        Competition(
            slug = "arg.3",
            name = "Primera B",
            region = Region.ARGENTINA,
            hasStandings = false
        ),
        Competition(
            slug = "arg.supercopa",
            name = "Supercopa Argentina",
            region = Region.ARGENTINA,
            hasStandings = false
        ),

        // ---- Conmebol ----
        Competition(
            slug = "conmebol.libertadores",
            name = "Copa Libertadores",
            region = Region.CONMEBOL,
            hasStandings = true,
            qualificationZones = listOf(
                qualifies(1, 2, "Octavos de final"),
                playoff(3, 3, "Pasa a Sudamericana"),
                QualificationZone(4, 4, "Eliminado", ZoneKind.ELIMINATED)
            )
        ),
        Competition(
            slug = "conmebol.sudamericana",
            name = "Copa Sudamericana",
            region = Region.CONMEBOL,
            hasStandings = true,
            qualificationZones = listOf(
                qualifies(1, 1, "Octavos de final"),
                playoff(2, 2, "Playoff"),
                QualificationZone(3, 4, "Eliminado", ZoneKind.ELIMINATED)
            )
        ),
        Competition(
            slug = "conmebol.recopa",
            name = "Recopa Sudamericana",
            region = Region.CONMEBOL,
            hasStandings = false
        ),

        // ---- Europa ----
        Competition(
            slug = "eng.1",
            name = "Premier League",
            region = Region.EUROPE,
            hasStandings = true,
            qualificationZones = listOf(
                qualifies(1, 4, "Champions League"),
                playoff(5, 5, "Europa League"),
                relegated(18, 20)
            )
        ),
        Competition(
            slug = "esp.1",
            name = "LaLiga",
            region = Region.EUROPE,
            hasStandings = true,
            qualificationZones = listOf(
                qualifies(1, 4, "Champions League"),
                playoff(5, 6, "Europa League"),
                relegated(18, 20)
            )
        ),
        Competition(
            slug = "ita.1",
            name = "Serie A",
            region = Region.EUROPE,
            hasStandings = true,
            qualificationZones = listOf(
                qualifies(1, 4, "Champions League"),
                playoff(5, 6, "Europa League"),
                relegated(18, 20)
            )
        ),
        Competition(
            slug = "ger.1",
            name = "Bundesliga",
            region = Region.EUROPE,
            hasStandings = true,
            qualificationZones = listOf(
                qualifies(1, 4, "Champions League"),
                playoff(5, 6, "Europa League"),
                QualificationZone(16, 16, "Promoción", ZoneKind.RELEGATION_PLAYOFF),
                relegated(17, 18)
            )
        ),
        Competition(
            slug = "fra.1",
            name = "Ligue 1",
            region = Region.EUROPE,
            hasStandings = true,
            qualificationZones = listOf(
                qualifies(1, 3, "Champions League"),
                playoff(4, 4, "Playoff Champions"),
                QualificationZone(16, 16, "Promoción", ZoneKind.RELEGATION_PLAYOFF),
                relegated(17, 18)
            )
        ),
        Competition(
            slug = "uefa.champions",
            name = "Champions League",
            region = Region.EUROPE,
            hasStandings = true,
            qualificationZones = listOf(
                qualifies(1, 8, "Octavos de final"),
                playoff(9, 24, "Playoff"),
                QualificationZone(25, 36, "Eliminado", ZoneKind.ELIMINATED)
            )
        ),
        Competition(
            slug = "uefa.europa",
            name = "Europa League",
            region = Region.EUROPE,
            hasStandings = true,
            qualificationZones = listOf(
                qualifies(1, 8, "Octavos de final"),
                playoff(9, 24, "Playoff"),
                QualificationZone(25, 36, "Eliminado", ZoneKind.ELIMINATED)
            )
        )
    )

    val byRegion: Map<Region, List<Competition>> = all.groupBy { it.region }

    fun bySlug(slug: String): Competition? = all.firstOrNull { it.slug == slug }

    /** Opened first when the app starts. */
    val default: Competition = all.first()
}
