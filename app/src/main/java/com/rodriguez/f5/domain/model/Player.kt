package com.rodriguez.f5.domain.model

/**
 * Squad member.
 *
 * The data source does not provide photographs for players of the Argentine
 * leagues, so [photoUrl] is null most of the time and the UI must read well
 * without it.
 */
data class Player(
    val id: String,
    val teamId: String,
    val name: String,
    val position: String,
    val shirtNumber: Int?,
    val age: Int?,
    val nationality: String?,
    val photoUrl: String?
)

/** Coarse grouping used to split a squad into sections. */
enum class PositionGroup(val label: String) {
    GOALKEEPER("Arqueros"),
    DEFENDER("Defensores"),
    MIDFIELDER("Mediocampistas"),
    FORWARD("Delanteros"),
    OTHER("Otros");

    companion object {
        /** Maps the abbreviations the API returns onto the four usual lines. */
        fun fromAbbreviation(abbreviation: String?): PositionGroup = when (abbreviation?.uppercase()) {
            "G", "GK" -> GOALKEEPER
            "D", "CB", "LB", "RB", "DF" -> DEFENDER
            "M", "CM", "DM", "AM", "MF" -> MIDFIELDER
            "F", "ST", "CF", "LW", "RW", "FW" -> FORWARD
            else -> OTHER
        }
    }
}
