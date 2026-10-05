package com.buildsol.mapassesment.routeScore.domain.usecase

import com.buildsol.mapassesment.routeScore.domain.model.Place
import com.buildsol.mapassesment.routeScore.domain.repository.RouteRepository

class SearchPlacesUseCase(
    private val repository: RouteRepository
) {

    suspend operator fun invoke(query: String): Result<List<Place>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }

        return repository.searchPlaces(query.trim())
    }
}