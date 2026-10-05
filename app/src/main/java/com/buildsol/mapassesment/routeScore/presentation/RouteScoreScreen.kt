package com.buildsol.mapassesment.routeScore.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildsol.mapassesment.routeScore.presentation.components.RouteMap
import com.buildsol.mapassesment.routeScore.presentation.components.RouteResultsPanel
import com.buildsol.mapassesment.routeScore.presentation.components.RouteSearchCard
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RouteScoreScreen(
    viewModel: RouteScoreViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is RouteScoreContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }

                RouteScoreContract.Effect.HideKeyboard -> {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        RouteScoreContent(
            state = state,
            onIntent = viewModel::onIntent,
            topPadding = paddingValues.calculateTopPadding()
        )
    }
}

@Composable
private fun RouteScoreContent(
    state: RouteScoreContract.State,
    onIntent: (RouteScoreContract.Intent) -> Unit,
    topPadding: androidx.compose.ui.unit.Dp
) {
    // Tell the map how much of the screen is covered so it can frame routes in the free area.
    // Tune these two numbers if you change the card or sheet heights.
    val hasPanel = state.loadState != RouteScoreContract.LoadState.Idle
    val bottomInset by animateDpAsState(
        targetValue = if (hasPanel) 300.dp else 48.dp,
        label = "mapBottomInset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        RouteMap(
            routes = state.routes,
            selectedRouteId = state.selectedRouteId,
            safestRouteId = state.safestRouteId,
            source = state.source.selectedPlace?.coordinate,
            destination = state.destination.selectedPlace?.coordinate,
            onRouteSelected = { onIntent(RouteScoreContract.Intent.RouteSelected(it)) },
            topInset = topPadding + 230.dp,
            bottomInset = bottomInset
        )

        RouteSearchCard(
            state = state,
            onIntent = onIntent,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = topPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        RouteResultsPanel(
            state = state,
            onIntent = onIntent,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
