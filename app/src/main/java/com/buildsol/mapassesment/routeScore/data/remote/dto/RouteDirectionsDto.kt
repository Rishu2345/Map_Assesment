package com.buildsol.mapassesment.routeScore.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteDirectionsDto(
    val code: String,
    val routes: List<DirectionsRouteDto> = emptyList()
)

@Serializable
data class DirectionsRouteDto(
    val distance: Double,
    val duration: Double,
    val geometry: String
)