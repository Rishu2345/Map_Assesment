package com.buildsol.mapassesment.routeScore.presentation.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.buildsol.mapassesment.routeScore.domain.model.Coordinate
import com.buildsol.mapassesment.routeScore.domain.model.Route
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min


@Composable
fun RouteMap(
    routes: List<Route>,
    selectedRouteId: String?,
    safestRouteId: String?,
    source: Coordinate?,
    destination: Coordinate?,
    onRouteSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    topInset: Dp = 0.dp,
    bottomInset: Dp = 0.dp
) {
    val viewportState = rememberMapViewportState()
    val density = LocalDensity.current
    var mapSize by remember { mutableStateOf(IntSize.Zero) }

    val colors = MaterialTheme.colorScheme
    val inactiveColor = colors.outline
    val primary = colors.primary
    val onPrimary = colors.onPrimary

    LaunchedEffect(source, destination, routes, mapSize, topInset, bottomInset) {
        if (mapSize == IntSize.Zero) return@LaunchedEffect

        val coordinates = buildList {
            source?.let(::add)
            destination?.let(::add)
            routes.forEach { addAll(it.geometry) }
        }
        if (coordinates.isEmpty()) return@LaunchedEffect

        val topPx = with(density) { topInset.toPx() }.toDouble()
        val bottomPx = with(density) { bottomInset.toPx() }.toDouble()
        val sidePx = with(density) { 40.dp.toPx() }.toDouble()

        val camera = fitCamera(
            coordinates = coordinates,
            viewportPx = mapSize,
            density = density.density.toDouble(),
            topPx = topPx,
            bottomPx = bottomPx,
            sidePx = sidePx
        )

        viewportState.easeTo(
            camera,
            MapAnimationOptions.mapAnimationOptions { duration(800L) }
        )
    }

    MapboxMap(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { mapSize = it },
        mapViewportState = viewportState
    ) {

        val ordered = routes.sortedWith(
            compareBy<Route> { it.id == selectedRouteId }.thenBy { it.id == safestRouteId }
        )

        ordered.forEach { route ->
            key(route.id) {
                val isSelected = route.id == selectedRouteId
                val points = remember(route.id) { route.geometry.map(Coordinate::toPoint) }
                val routeColor = scoreColor(route.score)

                if (isSelected) {
                    PolylineAnnotation(points = points) {
                        lineColor = Color.White
                        lineWidth = 11.0
                    }
                }

                PolylineAnnotation(points = points) {
                    lineColor = if (isSelected) routeColor else inactiveColor
                    lineWidth = if (isSelected) 7.0 else 4.0
                    lineOpacity = if (isSelected) 1.0 else 0.6

                    interactionsState.onClicked {
                        onRouteSelected(route.id)
                        true
                    }
                }
            }
        }

        source?.let {
            CircleAnnotation(point = it.toPoint()) {
                circleRadius = 8.0
                circleColor = onPrimary
                circleStrokeColor = primary
                circleStrokeWidth = 4.0
            }
        }

        destination?.let {
            CircleAnnotation(point = it.toPoint()) {
                circleRadius = 9.0
                circleColor = primary
                circleStrokeColor = onPrimary
                circleStrokeWidth = 3.0
            }
        }
    }
}

private fun Coordinate.toPoint(): Point = Point.fromLngLat(longitude, latitude)


private fun fitCamera(
    coordinates: List<Coordinate>,
    viewportPx: IntSize,
    density: Double,
    topPx: Double,
    bottomPx: Double,
    sidePx: Double
): CameraOptions {
    val minLat = coordinates.minOf { it.latitude }
    val maxLat = coordinates.maxOf { it.latitude }
    val minLon = coordinates.minOf { it.longitude }
    val maxLon = coordinates.maxOf { it.longitude }

    val centerLat = (minLat + maxLat) / 2
    val centerLon = (minLon + maxLon) / 2

    val availableWidthDp = ((viewportPx.width - 2 * sidePx) / density).coerceAtLeast(1.0)
    val availableHeightDp = ((viewportPx.height - topPx - bottomPx) / density).coerceAtLeast(1.0)

    val lonSpan = max(maxLon - minLon, 1e-4)
    val latSpan = max((maxLat - minLat) / cos(Math.toRadians(centerLat)), 1e-4)

    val zoomForWidth = log2(availableWidthDp * 360.0 / (512.0 * lonSpan))
    val zoomForHeight = log2(availableHeightDp * 360.0 / (512.0 * latSpan))
    val zoom = min(zoomForWidth, zoomForHeight).coerceIn(2.0, 15.5)

    return CameraOptions.Builder()
        .center(Point.fromLngLat(centerLon, centerLat))
        .zoom(zoom)
        .padding(EdgeInsets(topPx, sidePx, bottomPx, sidePx))
        .build()
}

private fun log2(value: Double): Double = ln(value) / ln(2.0)
