package com.buildsol.mapassesment

import android.app.Application
import androidx.core.os.BuildCompat
import com.buildsol.mapassesment.routeScore.di.PresentationModule
import com.buildsol.mapassesment.routeScore.di.routeScoreModule
import com.mapbox.common.MapboxOptions
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        MapboxOptions.accessToken = BuildConfig.MAPBOX_PUBLIC_TOKEN

        startKoin {
            androidContext(this@App)
            modules(routeScoreModule,
                PresentationModule
            )
        }
    }
}