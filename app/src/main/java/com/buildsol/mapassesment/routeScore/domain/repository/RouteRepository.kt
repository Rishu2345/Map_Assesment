package com.buildsol.mapassesment.routeScore.domain.repository

import com.buildsol.mapassesment.routeScore.domain.model.Coordinate
import com.buildsol.mapassesment.routeScore.domain.model.Place
import com.buildsol.mapassesment.routeScore.domain.model.Route

interface RouteRepository {
    suspend fun searchPlaces(query: String): Result<List<Place>>

    suspend fun findRoutes(
        origin: Coordinate,
        destination: Coordinate
    ): Result<List<Route>>
}