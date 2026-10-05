package com.buildsol.mapassesment.routeScore.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.buildsol.mapassesment.routeScore.domain.model.Route
import kotlin.math.roundToInt

@Composable
fun RouteCard(
    route: Route,
    routeIndex: Int,
    isSelected: Boolean,
    isSafest: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val accent = scoreColor(route.score)

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> accent
            isSafest -> colors.primary.copy(alpha = 0.5f)
            else -> colors.outlineVariant
        },
        label = "routeCardBorder"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        label = "routeCardBorderWidth"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) accent.copy(alpha = 0.08f).compositeOver(colors.surface)
        else colors.surface,
        label = "routeCardContainer"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.96f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "routeCardScale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Route ${'A' + routeIndex}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (isSafest) "Safest route" else "Alternative",
                        color = if (isSafest) colors.primary else colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                ScoreBadge(score = route.score)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                Stat(label = "Distance", value = formatDistance(route.distanceMeters))
                Stat(label = "Time", value = formatDuration(route.durationSeconds))
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private fun formatDistance(distanceMeters: Double): String {
    val km = (distanceMeters / 1000.0 * 10).roundToInt() / 10.0
    return "$km km"
}

private fun formatDuration(durationSeconds: Double): String {
    val totalMinutes = (durationSeconds / 60).roundToInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours > 0 && minutes == 0 -> "$hours hr"
        hours > 0 -> "$hours hr $minutes min"
        else -> "$minutes min"
    }
}
