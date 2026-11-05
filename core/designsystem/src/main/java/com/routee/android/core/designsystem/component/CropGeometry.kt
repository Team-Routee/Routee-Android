package com.routee.android.core.designsystem.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val MAX_ZOOM = 8f
private const val MIN_BOX_RATIO = 0.25f
private const val QUARTER_TURNS = 4

internal enum class CropHandle(
    val xSign: Int,
    val ySign: Int,
) {
    TopLeft(xSign = -1, ySign = -1),
    Top(xSign = 0, ySign = -1),
    TopRight(xSign = 1, ySign = -1),
    Left(xSign = -1, ySign = 0),
    Right(xSign = 1, ySign = 0),
    BottomLeft(xSign = -1, ySign = 1),
    Bottom(xSign = 0, ySign = 1),
    BottomRight(xSign = 1, ySign = 1),
}

internal data class CropGeometry(
    val contentBounds: Rect,
    val aspectRatio: Float,
    val imageSize: Size,
    val quarterTurns: Int,
    val box: Rect,
    val scale: Float,
    val center: Offset,
    val isSettled: Boolean,
) {
    private val fullBox: Rect
        get() = fittedBox(contentBounds, aspectRatio)

    private val rotatedImageSize: Size
        get() = if (quarterTurns % 2 == 0) imageSize else Size(imageSize.height, imageSize.width)

    private val maxScale: Float
        get() = coverScale(fullBox) * MAX_ZOOM

    val photoRect: Rect
        get() = Rect(center = center, radius = 0f).inflate(
            horizontal = rotatedImageSize.width * scale / 2,
            vertical = rotatedImageSize.height * scale / 2,
        )

    fun handlePosition(handle: CropHandle): Offset = Offset(
        x = box.center.x + handle.xSign * box.width / 2,
        y = box.center.y + handle.ySign * box.height / 2,
    )

    fun handleAt(
        point: Offset,
        touchRadius: Float,
    ): CropHandle? {
        if (!box.inflate(touchRadius).contains(point)) return null
        val xSign = when {
            abs(point.x - box.left) <= touchRadius -> -1
            abs(point.x - box.right) <= touchRadius -> 1
            else -> 0
        }
        val ySign = when {
            abs(point.y - box.top) <= touchRadius -> -1
            abs(point.y - box.bottom) <= touchRadius -> 1
            else -> 0
        }
        return CropHandle.entries.firstOrNull { it.xSign == xSign && it.ySign == ySign }
    }

    fun transformed(
        centroid: Offset,
        pan: Offset,
        zoom: Float,
    ): CropGeometry {
        if (zoom == 1f && pan == Offset.Zero) return this
        val newScale = (scale * zoom).coerceIn(coverScale(box), maxScale)
        val previousCentroid = centroid - pan
        val newCenter = centroid + (center - previousCentroid) * (newScale / scale)
        return copy(scale = newScale, center = newCenter).withPhotoCoveringBox()
    }

    fun resized(
        handle: CropHandle,
        target: Offset,
    ): CropGeometry {
        val anchor = Offset(
            x = box.center.x - handle.xSign * box.width / 2,
            y = box.center.y - handle.ySign * box.height / 2,
        )
        val widthRatio = (target.x - anchor.x) * handle.xSign / box.width
        val heightRatio = (target.y - anchor.y) * handle.ySign / box.height
        val requested = when {
            handle.xSign == 0 -> heightRatio
            handle.ySign == 0 -> widthRatio
            else -> (widthRatio + heightRatio) / 2
        }
        val minRatio = min(MIN_BOX_RATIO * fullBox.width / box.width, 1f)
        val ratio = requested.coerceIn(minRatio, maxResizeRatio(anchor))
        if (ratio == 1f) return this
        return copy(
            box = Rect(
                left = anchor.x + (box.left - anchor.x) * ratio,
                top = anchor.y + (box.top - anchor.y) * ratio,
                right = anchor.x + (box.right - anchor.x) * ratio,
                bottom = anchor.y + (box.bottom - anchor.y) * ratio,
            ),
            isSettled = false,
        )
    }

    fun recentered(): CropGeometry {
        val target = fullBox
        val ratio = min(target.width / box.width, maxScale / scale)
        val newBox = Rect(center = target.center, radius = 0f).inflate(
            horizontal = box.width * ratio / 2,
            vertical = box.height * ratio / 2,
        )
        return copy(
            box = newBox,
            scale = scale * ratio,
            center = newBox.center + (center - box.center) * ratio,
            isSettled = true,
        )
    }

    fun interpolatedTo(
        target: CropGeometry,
        fraction: Float,
    ): CropGeometry = copy(
        box = lerp(box, target.box, fraction),
        scale = scale + (target.scale - scale) * fraction,
        center = lerp(center, target.center, fraction),
    )

    fun rotated(clockwise: Boolean): CropGeometry {
        val settled = recentered()
        val offset = settled.center - settled.box.center
        val rotatedOffset = if (clockwise) Offset(-offset.y, offset.x) else Offset(offset.y, -offset.x)
        val turned = settled.copy(
            quarterTurns = (settled.quarterTurns + if (clockwise) 1 else QUARTER_TURNS - 1) % QUARTER_TURNS,
            center = settled.box.center + rotatedOffset,
        )
        return turned.copy(scale = turned.scale.coerceIn(turned.coverScale(turned.box), turned.maxScale))
            .withPhotoCoveringBox()
    }

    fun reset(): CropGeometry = initial(contentBounds, aspectRatio, imageSize)

    private fun coverScale(target: Rect): Float = max(
        target.width / rotatedImageSize.width,
        target.height / rotatedImageSize.height,
    )

    private fun withPhotoCoveringBox(): CropGeometry {
        val halfWidth = rotatedImageSize.width * scale / 2
        val halfHeight = rotatedImageSize.height * scale / 2
        return copy(
            center = Offset(
                x = center.x.coerceIn(box.right - halfWidth, box.left + halfWidth),
                y = center.y.coerceIn(box.bottom - halfHeight, box.top + halfHeight),
            ),
        )
    }

    private fun maxResizeRatio(anchor: Offset): Float {
        val allowed = contentBounds.intersect(photoRect)
        val ratio = listOf(
            (anchor.x - allowed.left) to (anchor.x - box.left),
            (allowed.right - anchor.x) to (box.right - anchor.x),
            (anchor.y - allowed.top) to (anchor.y - box.top),
            (allowed.bottom - anchor.y) to (box.bottom - anchor.y),
        ).filter { (_, current) -> current > 0f }
            .minOf { (limit, current) -> limit / current }
        return max(ratio, 1f)
    }

    companion object {
        fun initial(
            contentBounds: Rect,
            aspectRatio: Float,
            imageSize: Size,
        ): CropGeometry {
            val box = fittedBox(contentBounds, aspectRatio)
            return CropGeometry(
                contentBounds = contentBounds,
                aspectRatio = aspectRatio,
                imageSize = imageSize,
                quarterTurns = 0,
                box = box,
                scale = max(box.width / imageSize.width, box.height / imageSize.height),
                center = box.center,
                isSettled = true,
            )
        }

        private fun fittedBox(
            bounds: Rect,
            aspectRatio: Float,
        ): Rect {
            val width = min(bounds.width, bounds.height * aspectRatio)
            val height = width / aspectRatio
            return Rect(center = bounds.center, radius = 0f).inflate(
                horizontal = width / 2,
                vertical = height / 2,
            )
        }
    }
}

private fun Rect.inflate(
    horizontal: Float,
    vertical: Float,
): Rect = Rect(
    left = left - horizontal,
    top = top - vertical,
    right = right + horizontal,
    bottom = bottom + vertical,
)
