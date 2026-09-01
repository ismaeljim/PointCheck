package com.pointcheck.features.dashboard.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pointcheck.core.ui.components.PointCheckCard

@Composable
fun MetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    PointCheckCard(
        title = value,
        modifier = modifier,
        subtitle = label,
        icon = icon,
        isVertical = true,
        onClick = onClick ?: {},
        titleMaxLines = 1,
        titleOverflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
    )
}

@Composable
fun FinancialRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label, 
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(8.dp))
        Text(
            value, 
            style = MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.Bold, 
            color = valueColor,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ShimmerDashboard() {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(MaterialTheme.shapes.medium)
                .shimmerEffect()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.weight(1f).height(100.dp).clip(MaterialTheme.shapes.small).shimmerEffect())
            Box(Modifier.weight(1f).height(100.dp).clip(MaterialTheme.shapes.small).shimmerEffect())
        }
        repeat(3) {
            Box(Modifier.fillMaxWidth().height(56.dp).clip(MaterialTheme.shapes.small).shimmerEffect())
        }
    }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ), label = ""
    )

    background(
        brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(
                Color(0xFFEBEBF4),
                Color(0xFFF4F4F4),
                Color(0xFFEBEBF4),
            ),
            start = androidx.compose.ui.geometry.Offset(startOffsetX, 0f),
            end = androidx.compose.ui.geometry.Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}
