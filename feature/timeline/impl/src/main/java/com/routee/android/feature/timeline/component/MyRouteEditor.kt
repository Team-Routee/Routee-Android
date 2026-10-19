package com.routee.android.feature.timeline.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeRouteMarker
import com.routee.android.core.designsystem.component.RouteeSecondaryButton
import com.routee.android.core.designsystem.component.textfield.RouteeBasicTextField
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.timeline.R
import kotlin.math.roundToInt

private val RouteLeadingWidth = 28.dp
private val RouteRowMinHeight = 48.dp
private val RouteRowSpacing = 12.dp
private const val MAX_POINT_COUNT = 20
private const val MAX_POINT_NAME_LENGTH = 16

internal data class RoutePoint(
    val id: Long,
    val name: TextFieldState,
)

@Composable
internal fun MyRouteEditor(
    points: List<RoutePoint>,
    onMovePoint: (from: Int, to: Int) -> Unit,
    onRemovePoint: (Int) -> Unit,
    onAddPoint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing24),
    ) {
        MyRouteHeader(
            currentCount = points.size,
        )
        Column {
            points.forEachIndexed { index, point ->
                key(point.id) {
                    RoutePointItem(
                        index = index,
                        name = point.name,
                        pointCount = points.size,
                        onMovePoint = onMovePoint,
                        onRemovePoint = onRemovePoint,
                    )
                    if (index < points.lastIndex) RouteConnector()
                }
            }
        }
        if (points.size < MAX_POINT_COUNT) {
            RouteeSecondaryButton(
                text = stringResource(R.string.timeline_route_add_point),
                onClick = onAddPoint,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MyRouteHeader(
    currentCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.timeline_route_title),
            color = RouteeTheme.colors.white,
            style = RouteeTheme.typography.titleSb18,
            modifier = Modifier.padding(start = 2.5.dp),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_sort),
                contentDescription = null,
            )
            Text(
                text = stringResource(R.string.timeline_route_point_count, currentCount, MAX_POINT_COUNT),
                color = RouteeTheme.colors.grey300,
                style = RouteeTheme.typography.labelM14,
                modifier = Modifier.padding(horizontal = 2.5.dp),
            )
        }
    }
}

@Composable
private fun RoutePointItem(
    index: Int,
    name: TextFieldState,
    pointCount: Int,
    onMovePoint: (Int, Int) -> Unit,
    onRemovePoint: (Int) -> Unit,
) {
    val connectorHeightPx = with(LocalDensity.current) { RouteRowSpacing.toPx() }
    val currentOnMovePoint by rememberUpdatedState(onMovePoint)
    val focusManager = LocalFocusManager.current
    var rowHeightPx by remember { mutableFloatStateOf(0f) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val dragGesture = Modifier.pointerInput(index, pointCount, connectorHeightPx) {
        detectDragGesturesAfterLongPress(
            onDragStart = { dragging = true },
            onDragEnd = {
                val stepPx = rowHeightPx + connectorHeightPx
                val destination = (index + (dragOffset / stepPx).roundToInt())
                    .coerceIn(0, pointCount - 1)
                dragOffset = 0f
                dragging = false
                focusManager.clearFocus()
                if (destination != index) currentOnMovePoint(index, destination)
            },
            onDragCancel = {
                dragOffset = 0f
                dragging = false
            },
            onDrag = { change, amount ->
                change.consume()
                dragOffset += amount.y
            },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { rowHeightPx = it.height.toFloat() }
            .zIndex(if (dragging) 1f else 0f)
            .then(if (dragging) Modifier.graphicsLayer { translationY = dragOffset } else Modifier),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(RouteLeadingWidth)
                .height(RouteRowMinHeight),
            contentAlignment = Alignment.TopCenter,
        ) {
            RouteeRouteMarker(
                number = index + 1,
                modifier = dragGesture.padding(top = RouteeTheme.spacing.Spacing10),
            )
            if (index < pointCount - 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(width = 1.dp, height = 6.dp)
                        .background(RouteeTheme.colors.grey400),
                )
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = RouteRowMinHeight)
                .clip(RoundedCornerShape(RouteeTheme.radius.Radius12))
                .background(RouteeTheme.colors.grey800)
                .padding(RouteeTheme.spacing.Spacing12),
            horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing6),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_route_drag_handle_sm_grey),
                    contentDescription = stringResource(R.string.timeline_route_drag_handle_a11y, index + 1),
                    modifier = dragGesture,
                )
                RouteeBasicTextField(
                    state = name,
                    modifier = Modifier.weight(1f),
                    textStyle = RouteeTheme.typography.labelM14.copy(color = RouteeTheme.colors.white),
                    placeholder = stringResource(R.string.timeline_route_point_placeholder),
                    placeholderColor = RouteeTheme.colors.grey400,
                    maxLength = MAX_POINT_NAME_LENGTH,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    onKeyboardAction = { focusManager.clearFocus() },
                )
            }
            Image(
                imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_minus_circle_sm),
                contentDescription = stringResource(R.string.timeline_route_remove_point_a11y, index + 1),
                modifier = Modifier
                    .clickable(role = Role.Button) { onRemovePoint(index) },
            )
        }
    }
}

@Composable
private fun RouteConnector() {
    Box(
        modifier = Modifier.size(width = RouteLeadingWidth, height = RouteRowSpacing),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 1.dp, height = RouteRowSpacing)
                .background(RouteeTheme.colors.grey400, CircleShape),
        )
    }
}

@PreviewFontSizes
@Composable
private fun MyRouteEditorPreview() {
    RouteeTheme {
        val points = remember {
            listOf(
                RoutePoint(1, TextFieldState("창의문")),
                RoutePoint(2, TextFieldState("청운대")),
                RoutePoint(3, TextFieldState("말바위")),
            )
        }
        MyRouteEditor(
            points = points,
            onMovePoint = { _, _ -> },
            onRemovePoint = {},
            onAddPoint = {},
            modifier = Modifier.width(328.dp),
        )
    }
}
