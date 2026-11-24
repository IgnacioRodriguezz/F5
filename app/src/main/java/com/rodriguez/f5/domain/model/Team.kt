package com.rodriguez.f5.domain.model

/** A club, as shown in lists, tables and match rows. */
data class Team(
    val id: String,
    val name: String,
    val shortName: String,
    val crestUrl: String?
)
