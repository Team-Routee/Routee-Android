package com.routee.android.core.map

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import com.naver.maps.map.MapView

@Composable
internal fun MapViewLifecycle(
    mapView: MapView,
    savedState: Bundle?,
    onSave: (Bundle) -> Unit,
) {
    val context = LocalContext.current
    val currentOnSave by rememberUpdatedState(onSave)

    // 지도 생성과 파괴, 메모리가 부족하면 지도 캐시 정리
    DisposableEffect(mapView) {
        mapView.onCreate(savedState)
        val memoryCallbacks = mapView.memoryCallbacks()
        context.registerComponentCallbacks(memoryCallbacks)
        onDispose {
            context.unregisterComponentCallbacks(memoryCallbacks)
            mapView.onDestroy()
        }
    }

    // 화면이 보이면 그리기 시작, 안 보이면 멈춤
    LifecycleStartEffect(mapView) {
        mapView.onStart()
        onStopOrDispose { mapView.onStop() }
    }

    // 화면 조작 가능하면 재개, 벗어나면 멈추고 상태 저장
    LifecycleResumeEffect(mapView) {
        mapView.onResume()
        onPauseOrDispose {
            mapView.onPause()
            currentOnSave(Bundle().also(mapView::onSaveInstanceState))
        }
    }
}

private fun MapView.memoryCallbacks() = object : ComponentCallbacks2 {
    override fun onTrimMemory(level: Int) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) this@memoryCallbacks.onLowMemory()
    }

    override fun onConfigurationChanged(newConfig: Configuration) = Unit

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() = this@memoryCallbacks.onLowMemory()
}
