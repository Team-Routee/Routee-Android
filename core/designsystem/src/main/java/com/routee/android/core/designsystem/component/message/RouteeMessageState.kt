package com.routee.android.core.designsystem.component.message

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 전역 메시지 상태
 *
 * 새 메시지가 오면 떠 있던 메시지를 바로 닫고 교체한다. 화면에서는 [LocalRouteeMessageState]로 꺼내 쓴다
 *
 * ```kotlin
 * val messageState = LocalRouteeMessageState.current
 * val scope = rememberCoroutineScope()
 *
 * viewModel.sideEffect.CollectSideEffect { sideEffect ->
 *     when (sideEffect) {
 *         FeatureSideEffect.ShowDeleteToast -> messageState.showToast(text = deletedText)
 *         FeatureSideEffect.ShowUndoSnackbar -> scope.launch {
 *             viewModel.onUndoSnackbarResult(messageState.showSnackbar(text = undoText, actionLabel = cancelText))
 *         }
 *     }
 * }
 * ```
 */
@Stable
class RouteeMessageState internal constructor(
    private val scope: CoroutineScope,
    private val accessibilityManager: AccessibilityManager?,
) {
    /** 지금 떠 있는 메시지. 뒤로가기 두 번 종료처럼 노출 여부 확인에 쓴다 */
    var currentMessage: RouteeMessage? by mutableStateOf(null)
        private set

    private var currentResult: CompletableDeferred<RouteeMessageResult>? = null

    internal val anchors = mutableStateMapOf<Modifier.Node, MessageAnchor>()

    /**
     * 토스트를 띄운다
     *
     * 화면을 벗어나도 정해진 시간 동안 유지된다 (예: 삭제 후 이전 화면으로 이동)
     */
    fun showToast(
        text: String,
        duration: RouteeMessageDuration = RouteeMessageDuration.SHORT,
    ) {
        // 호출 즉시 상태 반영, 타임아웃 대기만 비동기
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            show(RouteeMessage.Toast(text = text, duration = duration))
        }
    }

    /**
     * 스낵바를 띄우고 닫힐 때까지 기다린다. [actionLabel]이 null이면 버튼 없이 띄운다
     *
     * 호출한 코루틴이 취소되면 결과 없이 닫히므로, 삭제 확정 같은 마무리는 ViewModel에서 처리한다
     */
    suspend fun showSnackbar(
        text: String,
        actionLabel: String? = null,
        duration: RouteeMessageDuration = RouteeMessageDuration.UNDO,
    ): RouteeMessageResult = show(RouteeMessage.Snackbar(text = text, actionLabel = actionLabel, duration = duration))

    /**
     * 스낵바 버튼 동작
     */
    fun performAction() {
        currentResult?.complete(RouteeMessageResult.ACTION_PERFORMED)
    }

    /**
     * 떠 있는 메시지를 바로 닫는다
     */
    fun dismiss() {
        currentResult?.complete(RouteeMessageResult.DISMISSED)
    }

    private suspend fun show(message: RouteeMessage): RouteeMessageResult {
        currentResult?.complete(RouteeMessageResult.DISMISSED)
        val result = CompletableDeferred<RouteeMessageResult>()
        currentResult = result
        currentMessage = message
        try {
            return withTimeoutOrNull(message.timeoutMillis()) { result.await() }
                ?: RouteeMessageResult.DISMISSED
        } finally {
            result.complete(RouteeMessageResult.DISMISSED)
            if (currentResult === result) {
                currentResult = null
                currentMessage = null
            }
        }
    }

    private fun RouteeMessage.timeoutMillis(): Long =
        accessibilityManager?.calculateRecommendedTimeoutMillis(
            originalTimeoutMillis = duration.millis,
            containsIcons = false,
            containsText = true,
            containsControls = this is RouteeMessage.Snackbar && actionLabel != null,
        ) ?: duration.millis
}

@Composable
fun rememberRouteeMessageState(): RouteeMessageState {
    val scope = rememberCoroutineScope()
    val accessibilityManager = LocalAccessibilityManager.current
    return remember(scope, accessibilityManager) { RouteeMessageState(scope, accessibilityManager) }
}

/**
 * 앱 루트에서 제공하는 전역 메시지 상태
 */
val LocalRouteeMessageState = staticCompositionLocalOf {
    RouteeMessageState(scope = MainScope(), accessibilityManager = null)
}
