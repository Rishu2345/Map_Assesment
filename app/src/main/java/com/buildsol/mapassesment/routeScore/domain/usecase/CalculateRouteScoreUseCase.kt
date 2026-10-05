package com.buildsol.mapassesment.routeScore.domain.usecase

import com.buildsol.mapassesment.routeScore.domain.model.Coordinate
import com.buildsol.mapassesment.routeScore.domain.model.RouteScore

class CalculateRouteScoreUseCase {

    operator fun invoke(
        geometry: List<Coordinate>,
        distanceMeters: Double,
        routeIndex: Int
    ): RouteScore {
        val geometryHash = stableGeometryHash(geometry)
        val distanceFactor = (distanceMeters / 1000.0).coerceIn(0.0, 100.0)

        val base = (geometryHash % 71) + 20
        val routeAdjustment = routeIndex * 3
        val distanceAdjustment = distanceFactor.toInt() % 12

        val score = (base - routeAdjustment - distanceAdjustment).coerceIn(0, 100)

        return RouteScore(score)
    }

    private fun stableGeometryHash(geometry: List<Coordinate>): Int {
        var hash = 17

        geometry.forEach { point ->
            hash = hash * 31 + point.latitude.toBits().hashCode()
            hash = hash * 31 + point.longitude.toBits().hashCode()
        }

        return hash.toUInt().toInt().let { kotlin.math.abs(it) }
    }
}