package com.buildsol.mapassesment.routeScore.data.remote.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GeocodingDto(
    val features: List<GeocodingFeatureDto> = emptyList()
)

@Serializable
data class GeocodingFeatureDto(
    val id: String,
    val geometry: GeometryDto,
    val properties: GeocodingPropertiesDto,
    val text: String? = null
)

@Serializable
data class GeometryDto(
    val coordinates: List<Double> = emptyList()
)


@Serializable
data class GeocodingPropertiesDto(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("full_address")
    val fullAddress: String? = null,

    @SerializedName("place_formatted")
    val placeFormatted: String? = null
)