package com.buildsol.mapassesment.routeScore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildsol.mapassesment.routeScore.domain.model.Place
import com.buildsol.mapassesment.routeScore.domain.model.Route
import com.buildsol.mapassesment.routeScore.domain.usecase.FindRoutesUseCase
import com.buildsol.mapassesment.routeScore.domain.usecase.SearchPlacesUseCase
import com.buildsol.mapassesment.routeScore.domain.usecase.SelectSafestRouteUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RouteScoreViewModel(
    private val searchPlaces: SearchPlacesUseCase,
    private val findRoutes: FindRoutesUseCase,
    private val selectSafestRoute: SelectSafestRouteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RouteScoreContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<RouteScoreContract.Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val searchRequests = MutableSharedFlow<SearchRequest>(
        extraBufferCapacity = 1
    )

    init {
        observePlaceSearch()
    }

    fun onIntent(intent: RouteScoreContract.Intent) {
        when (intent) {
            is RouteScoreContract.Intent.SourceQueryChanged -> {
                reduceSourceQuery(intent.query)
            }

            is RouteScoreContract.Intent.DestinationQueryChanged -> {
                reduceDestinationQuery(intent.query)
            }

            is RouteScoreContract.Intent.PlaceSelected -> {
                selectPlace(intent.place)
            }

            RouteScoreContract.Intent.SwapLocations -> {
                swapLocations()
            }

            RouteScoreContract.Intent.FindRoutes -> {
                findRoutes()
            }

            is RouteScoreContract.Intent.RouteSelected -> {
                selectRoute(intent.routeId)
            }

            RouteScoreContract.Intent.Retry -> {
                findRoutes()
            }

            RouteScoreContract.Intent.DismissError -> {
                dismissError()
            }
        }
    }

    private fun reduceSourceQuery(query: String) {
        _state.update {
            it.copy(
                source = it.source.copy(
                    query = query,
                    selectedPlace = null
                ),
                suggestions = emptyList(),
                activeField = RouteScoreContract.ActiveField.SOURCE
            )
        }

        searchRequests.tryEmit(
            SearchRequest(
                field = RouteScoreContract.ActiveField.SOURCE,
                query = query
            )
        )
    }

    private fun reduceDestinationQuery(query: String) {
        _state.update {
            it.copy(
                destination = it.destination.copy(
                    query = query,
                    selectedPlace = null
                ),
                suggestions = emptyList(),
                activeField = RouteScoreContract.ActiveField.DESTINATION
            )
        }

        searchRequests.tryEmit(
            SearchRequest(
                field = RouteScoreContract.ActiveField.DESTINATION,
                query = query
            )
        )
    }

    private fun observePlaceSearch() {
        viewModelScope.launch {
            searchRequests
                .debounce(300)
                .filter { it.query.trim().length >= 2 }
                .collectLatest { request ->
                    searchPlaces(request.query)
                        .onSuccess { places ->
                            _state.update { state ->
                                if (state.activeField != request.field) {
                                    state
                                } else {
                                    state.copy(suggestions = places)
                                }
                            }
                        }
                        .onFailure { error ->
                            _effect.send(
                                RouteScoreContract.Effect.ShowSnackbar(
                                    error.message ?: "Unable to search places"
                                )
                            )
                        }
                }
        }
    }

    private fun selectPlace(place: Place) {
        val activeField = _state.value.activeField

        _state.update {
            when (activeField) {
                RouteScoreContract.ActiveField.SOURCE -> {
                    it.copy(
                        source = RouteScoreContract.PlaceField(
                            query = place.name,
                            selectedPlace = place
                        ),
                        suggestions = emptyList(),
                        activeField = RouteScoreContract.ActiveField.NONE
                    )
                }

                RouteScoreContract.ActiveField.DESTINATION -> {
                    it.copy(
                        destination = RouteScoreContract.PlaceField(
                            query = place.name,
                            selectedPlace = place
                        ),
                        suggestions = emptyList(),
                        activeField = RouteScoreContract.ActiveField.NONE
                    )
                }

                RouteScoreContract.ActiveField.NONE -> it
            }
        }

        sendEffect(RouteScoreContract.Effect.HideKeyboard)
    }

    private fun swapLocations() {
        _state.update {
            it.copy(
                source = it.destination,
                destination = it.source,
                suggestions = emptyList(),
                activeField = RouteScoreContract.ActiveField.NONE,
                routes = emptyList(),
                selectedRouteId = null,
                safestRouteId = null,
                loadState = RouteScoreContract.LoadState.Idle,
                errorMessage = null
            )
        }
    }

    private fun findRoutes() {
        val currentState = _state.value
        val source = currentState.source.selectedPlace
        val destination = currentState.destination.selectedPlace

        if (source == null || destination == null) {
            sendEffect(
                RouteScoreContract.Effect.ShowSnackbar(
                    "Select both source and destination"
                )
            )
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    loadState = RouteScoreContract.LoadState.Loading,
                    errorMessage = null,
                    suggestions = emptyList(),
                    activeField = RouteScoreContract.ActiveField.NONE
                )
            }

            findRoutes(
                origin = source.coordinate,
                destination = destination.coordinate
            ).onSuccess { routes ->
                handleRoutesLoaded(routes)
            }.onFailure { error ->
                handleRouteError(error)
            }
        }

        sendEffect(RouteScoreContract.Effect.HideKeyboard)
    }

    private fun handleRoutesLoaded(routes: List<Route>) {
        if (routes.isEmpty()) {
            _state.update {
                it.copy(
                    routes = emptyList(),
                    selectedRouteId = null,
                    safestRouteId = null,
                    loadState = RouteScoreContract.LoadState.Error,
                    errorMessage = "No routes were found"
                )
            }
            return
        }

        val safestRoute = selectSafestRoute(routes)

        _state.update {
            it.copy(
                routes = routes,
                selectedRouteId = safestRoute?.id,
                safestRouteId = safestRoute?.id,
                loadState = RouteScoreContract.LoadState.Success,
                errorMessage = null
            )
        }

        if (routes.size == 1) {
            sendEffect(
                RouteScoreContract.Effect.ShowSnackbar(
                    "Alternative routes are unavailable for this trip"
                )
            )
        }
    }

    private fun handleRouteError(error: Throwable) {
        val message = error.message ?: "Unable to find routes"

        _state.update {
            it.copy(
                routes = emptyList(),
                selectedRouteId = null,
                safestRouteId = null,
                loadState = RouteScoreContract.LoadState.Error,
                errorMessage = message
            )
        }

        sendEffect(
            RouteScoreContract.Effect.ShowSnackbar(message)
        )
    }

    private fun selectRoute(routeId: String) {
        val routeExists = _state.value.routes.any { it.id == routeId }

        if (!routeExists) return

        _state.update {
            it.copy(selectedRouteId = routeId)
        }
    }

    private fun dismissError() {
        _state.update {
            it.copy(
                errorMessage = null,
                loadState = if (it.routes.isEmpty()) {
                    RouteScoreContract.LoadState.Idle
                } else {
                    RouteScoreContract.LoadState.Success
                }
            )
        }
    }

    private fun sendEffect(effect: RouteScoreContract.Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private data class SearchRequest(
        val field: RouteScoreContract.ActiveField,
        val query: String
    )
}