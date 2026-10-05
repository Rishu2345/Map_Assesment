package com.buildsol.mapassesment.routeScore.data.mapper

import com.buildsol.mapassesment.routeScore.data.remote.dto.DirectionsRouteDto
import com.buildsol.mapassesment.routeScore.data.remote.dto.GeocodingFeatureDto
import com.buildsol.mapassesment.routeScore.domain.model.Coordinate
import com.buildsol.mapassesment.routeScore.domain.model.Place
import com.buildsol.mapassesment.routeScore.domain.model.Route
import com.buildsol.mapassesment.routeScore.domain.model.RouteScore

fun DirectionsRouteDto.toDomain(id: String): Route {
    return Route(
        id = id,
        geometry = decodePolyline6(geometry),
        distanceMeters = distance,
        durationSeconds = duration,
        score = RouteScore(0)
    )
}

fun GeocodingFeatureDto.toDomain(): Place? {
    if (geometry.coordinates.size < 2) return null

    return Place(
        id = id,
        name = properties.name ?: text ?: "Unknown place",
        address = properties.fullAddress ?: properties.placeFormatted,
        coordinate = Coordinate(
            latitude = geometry.coordinates[1],
            longitude = geometry.coordinates[0]
        )
    )
}

private fun decodePolyline6(encoded: String): List<Coordinate> {
    val coordinates = mutableListOf<Coordinate>()

    var index = 0
    var latitude = 0
    var longitude = 0

    while (index < encoded.length) {
        var result = 0
        var shift = 0

        while (true) {
            val byte = encoded[index++].code - 63
            result = result or ((byte and 0x1f) shl shift)
            shift += 5
            if (byte < 0x20) break
        }

        latitude += if ((result and 1) != 0) {
            -(result shr 1) - 1
        } else {
            result shr 1
        }

        result = 0
        shift = 0

        while (true) {
            val byte = encoded[index++].code - 63
            result = result or ((byte and 0x1f) shl shift)
            shift += 5
            if (byte < 0x20) break
        }

        longitude += if ((result and 1) != 0) {
            -(result shr 1) - 1
        } else {
            result shr 1
        }

        coordinates += Coordinate(
            latitude = latitude / 1_000_000.0,
            longitude = longitude / 1_000_000.0
        )
    }

    return coordinates
}