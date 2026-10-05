package com.buildsol.mapassesment.routeScore.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.buildsol.mapassesment.routeScore.domain.model.Place
import com.buildsol.mapassesment.routeScore.presentation.RouteScoreContract

@Composable
fun RouteSearchCard(
    state: RouteScoreContract.State,
    onIntent: (RouteScoreContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading = state.loadState == RouteScoreContract.LoadState.Loading
    val sourceReady = state.source.selectedPlace != null
    val destinationReady = state.destination.selectedPlace != null

    // Each tap on swap adds a half turn, so the icon keeps spinning the same way.
    var swapCount by remember { mutableIntStateOf(0) }
    val swapRotation by animateFloatAsState(
        targetValue = swapCount * 180f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "swapRotation"
    )

    // Keep the last non-empty list so the dropdown can animate out with content.
    var lastSuggestions by remember { mutableStateOf<List<Place>>(emptyList()) }
    if (state.suggestions.isNotEmpty()) lastSuggestions = state.suggestions

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RouteTimeline(
                        sourceReady = sourceReady,
                        destinationReady = destinationReady,
                        modifier = Modifier
                            .width(20.dp)
                            .fillMaxHeight()
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlaceField(
                            value = state.source.query,
                            label = "From",
                            imeAction = ImeAction.Next,
                            onValueChange = {
                                onIntent(RouteScoreContract.Intent.SourceQueryChanged(it))
                            }
                        )
                        PlaceField(
                            value = state.destination.query,
                            label = "To",
                            imeAction = ImeAction.Done,
                            onValueChange = {
                                onIntent(RouteScoreContract.Intent.DestinationQueryChanged(it))
                            }
                        )
                    }

                    IconButton(
                        onClick = {
                            swapCount++
                            onIntent(RouteScoreContract.Intent.SwapLocations)
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Swap source and destination"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = null,
                            modifier = Modifier.rotate(swapRotation)
                        )
                    }
                }

                Button(
                    onClick = { onIntent(RouteScoreContract.Intent.FindRoutes) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = sourceReady && destinationReady && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Scoring routes")
                    } else {
                        Text("Find safest route")
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = state.suggestions.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Suggestions(
                    places = lastSuggestions,
                    onPlaceSelected = {
                        onIntent(RouteScoreContract.Intent.PlaceSelected(it))
                    }
                )
            }
        }
    }
}

@Composable
private fun PlaceField(
    value: String,
    label: String,
    imeAction: ImeAction,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.semantics {
                        contentDescription = "Clear $label"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
            }
        }
    )
}

/**
 * Canvas timeline: hollow dot -> dashed line -> filled dot.
 * Each end fills with the primary color once a place is picked for that field.
 */
@Composable
private fun RouteTimeline(
    sourceReady: Boolean,
    destinationReady: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val sourceColor by animateColorAsState(
        targetValue = if (sourceReady) colors.primary else colors.outline,
        label = "timelineSource"
    )
    val destinationColor by animateColorAsState(
        targetValue = if (destinationReady) colors.primary else colors.outline,
        label = "timelineDestination"
    )
    val lineColor = colors.outlineVariant

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val fieldCenter = 28.dp.toPx() // half of a 56dp text field
        val topY = fieldCenter
        val bottomY = size.height - fieldCenter
        val radius = 6.dp.toPx()
        val stroke = 2.5.dp.toPx()

        drawLine(
            color = lineColor,
            start = Offset(centerX, topY + radius + 4.dp.toPx()),
            end = Offset(centerX, bottomY - radius - 4.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )

        // Source: hollow, becomes filled when selected
        drawCircle(
            color = sourceColor,
            radius = radius,
            center = Offset(centerX, topY),
            style = if (sourceReady) androidx.compose.ui.graphics.drawscope.Fill else Stroke(stroke)
        )

        // Destination: always filled, color shows readiness
        drawCircle(
            color = destinationColor,
            radius = radius,
            center = Offset(centerX, bottomY)
        )
    }
}

@Composable
private fun Suggestions(
    places: List<Place>,
    onPlaceSelected: (Place) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        places.take(5).forEach { place ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlaceSelected(place) }
                    .semantics { contentDescription = "Select ${place.name}" },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = {
                    Text(text = place.name, maxLines = 1)
                },
                supportingContent = {
                    place.address?.let {
                        Text(text = it, maxLines = 1)
                    }
                },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            )
        }
    }
}
