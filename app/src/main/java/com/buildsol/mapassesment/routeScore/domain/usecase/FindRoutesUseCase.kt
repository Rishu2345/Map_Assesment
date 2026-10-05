package com.buildsol.mapassesment.routeScore.domain.usecase

import com.buildsol.mapassesment.routeScore.domain.model.Coordinate
import com.buildsol.mapassesment.routeScore.domain.model.Route
import com.buildsol.mapassesment.routeScore.domain.repository.RouteRepository

class FindRoutesUseCase(
    private val repository: RouteRepository,
    private val calculateRouteScore: CalculateRouteScoreUseCase
) {

    suspend operator fun invoke(
        origin: Coordinate,
        destination: Coordinate
    ): Result<List<Route>> {
        return repository.findRoutes(origin, destination).map { routes ->
            routes.mapIndexed { index, route ->
                route.copy(
                    score = calculateRouteScore(
                        geometry = route.geometry,
                        distanceMeters = route.distanceMeters,
                        routeIndex = index
                    )
                )
            }
        }
    }
}