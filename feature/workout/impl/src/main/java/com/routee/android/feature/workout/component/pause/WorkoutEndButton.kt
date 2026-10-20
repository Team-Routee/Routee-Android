package com.routee.android.feature.workout.component.pause

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.lottie.RouteeLottie
import com.routee.android.feature.workout.R

private val ButtonSize = 60.dp
private val EndingLottieWidth = ButtonSize * 2.2f
private val EndingLottieHeight = EndingLottieWidth * (73f / 129f)

private const val LONG_PRESS_MILLIS = 1_500L

// 0~0.4초 없음, 0.4~0.7초 진동, 0.7~1.0초 쉼, 1.0~1.5초 진동
private val EndPressVibrationTimings = longArrayOf(400, 300, 300, 500)
private val EndPressVibrationAmplitudes = intArrayOf(0, 77, 0, 64)

/**
 * 운동 종료 버튼 (btn_end_main)
 *
 * @param onClick     1.5초 전에 손을 떼면 호출
 * @param onLongPress 1.5초 누르면 호출
 */
@Composable
internal fun WorkoutEndButton(
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vibrator = rememberVibrator()
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongPress by rememberUpdatedState(onLongPress)
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(ButtonSize)
            .background(RouteeTheme.colors.black, CircleShape)
            .endPressGesture(
                onPressChange = { pressed ->
                    isPressed = pressed
                    if (pressed) vibrator?.vibrateEndPress() else vibrator?.cancel()
                },
                onTap = { currentOnClick() },
                onLongPress = { currentOnLongPress() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.workout_pause_control_end),
            style = RouteeTheme.typography.labelSb16,
            color = RouteeTheme.colors.lime400,
            maxLines = 1,
        )
        if (isPressed) {
            RouteeLottie(
                resId = DesignSystemR.raw.lottie_workout_finish_button,
                modifier = Modifier.requiredSize(width = EndingLottieWidth, height = EndingLottieHeight),
                durationMillis = LONG_PRESS_MILLIS.toInt(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

private enum class EndPressResult { TAP, LONG_PRESS, CANCEL }

private fun Modifier.endPressGesture(
    onPressChange: (Boolean) -> Unit,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
): Modifier = this
    .semantics {
        role = Role.Button
        onClick {
            onTap()
            true
        }
        onLongClick {
            onLongPress()
            true
        }
    }
    .pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown()
            onPressChange(true)
            val result = awaitEndPressResult()
            onPressChange(false)
            when (result) {
                EndPressResult.TAP -> onTap()
                EndPressResult.LONG_PRESS -> onLongPress()
                EndPressResult.CANCEL -> Unit
            }
        }
    }

private suspend fun AwaitPointerEventScope.awaitEndPressResult(): EndPressResult =
    withTimeoutOrNull(LONG_PRESS_MILLIS) {
        if (waitForUpOrCancellation() != null) EndPressResult.TAP else EndPressResult.CANCEL
    } ?: EndPressResult.LONG_PRESS

@Composable
private fun rememberVibrator(): Vibrator? {
    val context = LocalContext.current
    return remember(context) { context.vibrator() }
}

private fun Context.vibrator(): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        getSystemService(Vibrator::class.java)
    }

private fun Vibrator.vibrateEndPress() {
    if (!hasVibrator()) return
    val effect = VibrationEffect.createWaveform(EndPressVibrationTimings, EndPressVibrationAmplitudes, -1)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH))
    } else {
        vibrate(effect)
    }
}

@PreviewFontSizes
@Composable
private fun WorkoutEndButtonPreview() {
    RouteePreviewTheme(modifier = Modifier.padding(16.dp)) {
        WorkoutEndButton(onClick = {}, onLongPress = {})
    }
}
