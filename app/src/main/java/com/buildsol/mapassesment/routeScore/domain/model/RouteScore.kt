package com.buildsol.mapassesment.routeScore.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@JvmInline
value class RouteScore(val value: Int) {
    init {
        require(value in 0..100) { "Route score must be between 0 and 100" }
    }
}