package com.buildsol.mapassesment.routeScore.data.remote.api

import com.buildsol.mapassesment.routeScore.data.remote.dto.GeocodingDto
import com.buildsol.mapassesment.routeScore.data.remote.dto.RouteDirectionsDto
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface RouteScoreApi {

    @GET
    suspend fun getDirections(
        @Url url: String,
        @Query("alternatives") alternatives: Boolean = true,
        @Query("geometries") geometries: String = "polyline6",
        @Query("overview") overview: String = "full",
        @Query("access_token") accessToken: String
    ): RouteDirectionsDto

    @GET("search/geocode/v6/forward")
    suspend fun searchPlaces(
        @Query("q") query: String,
        @Query("access_token") accessToken: String,
        @Query("limit") limit: Int = 5,
        @Query("autocomplete") autocomplete: Boolean = true,
        @Query("country") country: String = "in"
    ): GeocodingDto
}