package com.buildsol.mapassesment.routeScore.domain.model

data class Route(
    val id: String,
    val geometry: List<Coordinate>,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val score: RouteScore
)