package com.buildsol.mapassesment.routeScore.di

import com.buildsol.mapassesment.routeScore.presentation.RouteScoreViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val  PresentationModule = module{
    viewModelOf(::RouteScoreViewModel)
}