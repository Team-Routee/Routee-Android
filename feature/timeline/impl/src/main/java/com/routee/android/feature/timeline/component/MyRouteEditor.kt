package com.routee.android.feature.timeline.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val RouteLeadingWidth = 28.dp
private val RouteRowMinHeight = 48.dp
private val RouteRowSpacing = 12.dp
private const val MAX_POINT_COUNT = 20
private const val MAX_POINT_NAME_LENGTH = 16
private const val ROUTE_ANIMATION_DURATION_MS = 160

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
    val currentOnMovePoint by rememberUpdatedState(onMovePoint)
    val currentOnRemovePoint by rememberUpdatedState(onRemovePoint)
    val knownPointIds = remember { points.mapTo(mutableSetOf()) { it.id } }
    SideEffect { points.forEach { knownPointIds.add(it.id) } }
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var targetIndex by remember { mutableIntStateOf(-1) }
    var dragStepPx by remember { mutableFloatStateOf(0f) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var dragVersion by remember { mutableIntStateOf(0) }
    var settling by remember { mutableStateOf(false) }
    var removingPointId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing24),
    ) {
        MyRouteHeader(
            currentCount = points.size,
        )
        Column {
            points.forEachIndexed { index, point ->
                key(point.id, dragVersion) {
                    val visibilityState = remember {
                        MutableTransitionState(point.id in knownPointIds).apply { targetState = true }
                    }
                    if (removingPointId == point.id) {
                        LaunchedEffect(visibilityState) {
                            snapshotFlow { visibilityState.isIdle && !visibilityState.currentState }.first { it }
                            points.indexOfFirst { it.id == point.id }
                                .takeIf { it >= 0 }
                                ?.let(currentOnRemovePoint)
                            knownPointIds.remove(point.id)
                            removingPointId = null
                        }
                    }
                    val shiftPx by animateFloatAsState(
                        targetValue = when {
                            index in (draggedIndex + 1)..targetIndex -> -dragStepPx
                            index in targetIndex until draggedIndex -> dragStepPx
                            else -> 0f
                        },
                        animationSpec = tween(ROUTE_ANIMATION_DURATION_MS),
                        label = "RoutePointShift",
                    )
                    AnimatedVisibility(
                        visibleState = visibilityState,
                        enter = expandVertically(tween(ROUTE_ANIMATION_DURATION_MS)) +
                            fadeIn(tween(ROUTE_ANIMATION_DURATION_MS)),
                        exit = shrinkVertically(tween(ROUTE_ANIMATION_DURATION_MS)) +
                            fadeOut(tween(ROUTE_ANIMATION_DURATION_MS)),
                        modifier = Modifier
                            .zIndex(if (index == draggedIndex) 1f else 0f)
                            .offset {
                                IntOffset(0, (if (index == draggedIndex) dragOffsetPx else shiftPx).roundToInt())
                            },
                    ) {
                        Column {
                            RoutePointItem(
                                index = index,
                                name = point.name,
                                pointCount = points.size,
                                onDragStart = { stepPx ->
                                    if (!settling && draggedIndex == -1 && removingPointId == null) {
                                        draggedIndex = index
                                        targetIndex = index
                                        dragStepPx = stepPx
                                        dragOffsetPx = 0f
                                    }
                                },
                                onDrag = { delta ->
                                    if (!settling && draggedIndex == index) {
                                        dragOffsetPx += delta
                                        targetIndex = (draggedIndex + (dragOffsetPx / dragStepPx).roundToInt())
                                            .coerceIn(0, points.lastIndex)
                                    }
                                },
                                onDragEnd = {
                                    if (!settling && draggedIndex == index) {
                                        settling = true
                                        val from = draggedIndex
                                        val to = targetIndex
                                        scope.launch {
                                            animate(
                                                initialValue = dragOffsetPx,
                                                targetValue = (to - from) * dragStepPx,
                                                animationSpec = tween(ROUTE_ANIMATION_DURATION_MS),
                                            ) { value, _ ->
                                                dragOffsetPx = value
                                            }
                                            if (
                                                from != to &&
                                                points.getOrNull(from)?.id == point.id &&
                                                to in points.indices
                                            ) {
                                                currentOnMovePoint(from, to)
                                                dragVersion++
                                            }
                                            draggedIndex = -1
                                            targetIndex = -1
                                            dragOffsetPx = 0f
                                            settling = false
                                        }
                                    }
                                },
                                onDragCancel = {
                                    if (!settling && draggedIndex == index) {
                                        draggedIndex = -1
                                        targetIndex = -1
                                        dragOffsetPx = 0f
                                    }
                                },
                                onRemovePoint = {
                                    if (removingPointId == null && draggedIndex == -1 && !settling) {
                                        removingPointId = point.id
                                        visibilityState.targetState = false
                                    }
                                },
                            )
                            if (index < points.lastIndex) RouteConnector()
                        }
                    }
                }
            }
        }
        if (points.size < MAX_POINT_COUNT) {
            RouteeSecondaryButton(
                text = stringResource(R.string.timeline_route_add_point),
                onClick = {
                    if (draggedIndex == -1 && !settling && removingPointId == null) onAddPoint()
                },
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
    onDragStart: (Float) -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onRemovePoint: () -> Unit,
) {
    val connectorHeightPx = with(LocalDensity.current) { RouteRowSpacing.toPx() }
    val focusManager = LocalFocusManager.current
    var rowHeightPx by remember { mutableFloatStateOf(0f) }
    val dragGesture = Modifier.pointerInput(index, pointCount, connectorHeightPx) {
        detectDragGesturesAfterLongPress(
            onDragStart = { onDragStart(rowHeightPx + connectorHeightPx) },
            onDragEnd = {
                focusManager.clearFocus()
                onDragEnd()
            },
            onDragCancel = onDragCancel,
            onDrag = { change, amount ->
                change.consume()
                onDrag(amount.y)
            },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { rowHeightPx = it.height.toFloat() },
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
                    .clickable(role = Role.Button, onClick = onRemovePoint),
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
            ).toMutableStateList()
        }
        MyRouteEditor(
            points = points,
            onMovePoint = { from, to -> points.add(to, points.removeAt(from)) },
            onRemovePoint = points::removeAt,
            onAddPoint = {
                points.add(RoutePoint((points.maxOfOrNull { it.id } ?: -1L) + 1L, TextFieldState()))
            },
            modifier = Modifier.width(328.dp),
        )
    }
}
