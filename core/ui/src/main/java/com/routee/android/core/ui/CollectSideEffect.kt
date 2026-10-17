package com.routee.android.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * 화면이 보일 때(STARTED)만 사이드 이펙트를 수집한다
 *
 * Route에서 ViewModel의 `sideEffect`에 붙여 쓴다. 백그라운드에서는 수집을 멈추고, 돌아오면 다시 받는다
 *
 * ```kotlin
 * viewModel.sideEffect.CollectSideEffect { sideEffect ->
 *     when (sideEffect) { ... }
 * }
 * ```
 */
@Composable
fun <T> Flow<T>.CollectSideEffect(onSideEffect: suspend (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnSideEffect by rememberUpdatedState(onSideEffect)

    LaunchedEffect(this, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect { currentOnSideEffect(it) }
        }
    }
}
