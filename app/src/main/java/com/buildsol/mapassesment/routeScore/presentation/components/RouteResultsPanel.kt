package com.buildsol.mapassesment.routeScore.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.buildsol.mapassesment.routeScore.presentation.RouteScoreContract

private val CardWidth = 280.dp


@Composable
fun RouteResultsPanel(
    state: RouteScoreContract.State,
    onIntent: (RouteScoreContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.loadState != RouteScoreContract.LoadState.Idle,
        modifier = modifier,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            tonalElevation = 3.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .animateContentSize()
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 12.dp)
                        .align(Alignment.CenterHorizontally)
                        .width(36.dp)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )

                AnimatedContent(
                    targetState = state.loadState,
                    transitionSpec = {
                        (fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 6 }) togetherWith
                                fadeOut(tween(120))
                    },
                    label = "routePanelContent"
                ) { loadState ->
                    when (loadState) {
                        RouteScoreContract.LoadState.Loading -> SkeletonRow()

                        RouteScoreContract.LoadState.Error -> ErrorCard(
                            message = state.errorMessage ?: "Unable to find routes",
                            onRetry = { onIntent(RouteScoreContract.Intent.Retry) },
                            onDismiss = { onIntent(RouteScoreContract.Intent.DismissError) }
                        )

                        RouteScoreContract.LoadState.Success -> ResultsRow(
                            state = state,
                            onIntent = onIntent
                        )

                        RouteScoreContract.LoadState.Idle -> Box(Modifier.height(0.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsRow(
    state: RouteScoreContract.State,
    onIntent: (RouteScoreContract.Intent) -> Unit
) {
    val listState = rememberLazyListState()

    // Selecting a line on the map brings its card into view.
    LaunchedEffect(state.selectedRouteId, state.routes) {
        val index = state.routes.indexOfFirst { it.id == state.selectedRouteId }
        if (index >= 0) listState.animateScrollToItem(index)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = if (state.routes.size == 1) "1 route found" else "${state.routes.size} routes found",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Tap a card or a line on the map to compare",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        LazyRow(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = state.routes,
                key = { _, route -> route.id }
            ) { index, route ->
                RouteCard(
                    route = route,
                    routeIndex = index,
                    isSelected = route.id == state.selectedRouteId,
                    isSafest = route.id == state.safestRouteId,
                    onClick = {
                        onIntent(RouteScoreContract.Intent.RouteSelected(route.id))
                    },
                    modifier = Modifier.width(CardWidth)
                )
            }
        }
    }
}

@Composable
private fun SkeletonRow() {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "skeletonAlpha"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Bone(width = 120.dp, height = 18.dp, alpha = alpha)
            Bone(width = 200.dp, height = 12.dp, alpha = alpha)
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(2) { SkeletonCard(alpha) }
        }
    }
}

@Composable
private fun SkeletonCard(alpha: Float) {
    Surface(
        modifier = Modifier.width(CardWidth),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Bone(width = 90.dp, height = 18.dp, alpha = alpha)
                    Bone(width = 70.dp, height = 12.dp, alpha = alpha)
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                            CircleShape
                        )
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                Bone(width = 64.dp, height = 30.dp, alpha = alpha)
                Bone(width = 64.dp, height = 30.dp, alpha = alpha)
            }
        }
    }
}

@Composable
private fun Bone(width: Dp, height: Dp, alpha: Float) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                RoundedCornerShape(6.dp)
            )
    )
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Couldn't score routes",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Dismiss") }
                Button(onClick = onRetry) { Text("Try again") }
            }
        }
    }
}
