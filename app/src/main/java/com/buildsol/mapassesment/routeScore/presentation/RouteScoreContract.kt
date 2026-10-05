package com.buildsol.mapassesment.routeScore.presentation

import com.buildsol.mapassesment.routeScore.domain.model.Place
import com.buildsol.mapassesment.routeScore.domain.model.Route

object RouteScoreContract {

    data class State(
        val source: PlaceField = PlaceField(),
        val destination: PlaceField = PlaceField(),
        val suggestions: List<Place> = emptyList(),
        val activeField: ActiveField = ActiveField.NONE,
        val routes: List<Route> = emptyList(),
        val selectedRouteId: String? = null,
        val safestRouteId: String? = null,
        val loadState: LoadState = LoadState.Idle,
        val errorMessage: String? = null
    )

    data class PlaceField(
        val query: String = "",
        val selectedPlace: Place? = null
    )

    enum class ActiveField {
        SOURCE,
        DESTINATION,
        NONE
    }

    sealed interface LoadState {
        data object Idle : LoadState
        data object Loading : LoadState
        data object Success : LoadState
        data object Error : LoadState
    }

    sealed interface Intent {
        data class SourceQueryChanged(val query: String) : Intent
        data class DestinationQueryChanged(val query: String) : Intent
        data class PlaceSelected(val place: Place) : Intent
        data object SwapLocations : Intent
        data object FindRoutes : Intent
        data class RouteSelected(val routeId: String) : Intent
        data object Retry : Intent
        data object DismissError : Intent
    }

    sealed interface Effect {
        data class ShowSnackbar(val message: String) : Effect
        data object HideKeyboard : Effect
    }
}