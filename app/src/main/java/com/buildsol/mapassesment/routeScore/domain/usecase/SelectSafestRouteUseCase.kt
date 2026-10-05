package com.buildsol.mapassesment.routeScore.domain.usecase

import com.buildsol.mapassesment.routeScore.domain.model.Route

class SelectSafestRouteUseCase {

    operator fun invoke(routes: List<Route>): Route? {
        return routes.maxWithOrNull(
            compareBy<Route> { it.score.value }
                .thenByDescending { -it.durationSeconds }
        )
    }
}