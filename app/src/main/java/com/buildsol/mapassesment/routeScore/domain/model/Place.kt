package com.buildsol.mapassesment.routeScore.domain.model

data class Place(
    val id: String,
    val name: String,
    val address: String?,
    val coordinate: Coordinate
)