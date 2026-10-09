package com.routee.android.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush

// radial-gradient(50% 50% at 50% 50%)
@Immutable
internal class OvalRadialGradient(
    private val colors: List<Color>,
) : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        val radius = size.width / 2f
        val shader = RadialGradientShader(center = size.center, radius = radius, colors = colors)
        if (size.width > 0f) {
            shader.setLocalMatrix(
                android.graphics.Matrix().apply {
                    setScale(1f, size.height / size.width, size.center.x, size.center.y)
                },
            )
        }
        return shader
    }

    override fun equals(other: Any?): Boolean = other is OvalRadialGradient && colors == other.colors

    override fun hashCode(): Int = colors.hashCode()
}
