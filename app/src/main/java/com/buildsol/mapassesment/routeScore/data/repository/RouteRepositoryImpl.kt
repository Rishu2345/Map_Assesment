package com.buildsol.mapassesment.routeScore.data.repository


import com.buildsol.mapassesment.routeScore.data.mapper.toDomain
import com.buildsol.mapassesment.routeScore.data.remote.api.RouteScoreApi
import com.buildsol.mapassesment.routeScore.domain.model.Coordinate
import com.buildsol.mapassesment.routeScore.domain.model.Place
import com.buildsol.mapassesment.routeScore.domain.model.Route
import com.buildsol.mapassesment.routeScore.domain.repository.RouteRepository

class RouteRepositoryImpl(
    private val api: RouteScoreApi,
    private val accessToken: String
) : RouteRepository {

    override suspend fun searchPlaces(query: String): Result<List<Place>> {
        return runCatching {
            api.searchPlaces(
                query = query,
                accessToken = accessToken
            ).features.mapNotNull { it.toDomain() }
        }
    }

    override suspend fun findRoutes(
        origin: Coordinate,
        destination: Coordinate
    ): Result<List<Route>> {
        return runCatching {
            val coordinates = buildString {
                append(origin.longitude)
                append(",")
                append(origin.latitude)
                append(";")
                append(destination.longitude)
                append(",")
                append(destination.latitude)
            }

            val response = api.getDirections(
                url = "https://api.mapbox.com/directions/v5/mapbox/driving/$coordinates",
                accessToken = accessToken
            )

            check(response.code == "Ok") {
                "Mapbox returned ${response.code}"
            }

            response.routes.mapIndexed { index, route ->
                route.toDomain("route-$index")
            }
        }
    }
}