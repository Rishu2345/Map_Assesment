package com.buildsol.mapassesment.routeScore.di


import com.buildsol.mapassesment.BuildConfig
import com.buildsol.mapassesment.routeScore.data.remote.api.RouteScoreApi
import com.buildsol.mapassesment.routeScore.data.repository.RouteRepositoryImpl
import com.buildsol.mapassesment.routeScore.domain.repository.RouteRepository
import com.buildsol.mapassesment.routeScore.domain.usecase.CalculateRouteScoreUseCase
import com.buildsol.mapassesment.routeScore.domain.usecase.FindRoutesUseCase
import com.buildsol.mapassesment.routeScore.domain.usecase.SearchPlacesUseCase
import com.buildsol.mapassesment.routeScore.domain.usecase.SelectSafestRouteUseCase
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

private const val MAPBOX_BASE_URL = "https://api.mapbox.com/"

val routeScoreModule = module {


    single<OkHttpClient>{
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }



    single {
        val json = Json{
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        Retrofit.Builder()
            .baseUrl(MAPBOX_BASE_URL)
            .client(get())
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
            .create<RouteScoreApi>()
    }

    single<RouteRepository> {
        RouteRepositoryImpl(
            api = get(),
            accessToken = BuildConfig.MAPBOX_PUBLIC_TOKEN
        )
    }

    factory {
        CalculateRouteScoreUseCase()
    }

    factory {
        SelectSafestRouteUseCase()
    }

    factory {
        SearchPlacesUseCase(get())
    }

    factory {
        FindRoutesUseCase(
            repository = get(),
            calculateRouteScore = get()
        )
    }
}