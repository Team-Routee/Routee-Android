package com.routee.android

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.network.session.AuthSessionManager
import com.routee.android.feature.onboarding.api.LoginNavKey
import com.routee.android.feature.workout.api.WorkoutMainNavKey
import com.routee.android.ui.RouteeApp
import com.routee.android.util.lockPortraitOnPhone
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authSessionManager: AuthSessionManager

    // 로그인 세션을 확인하기 전에는 null, 그동안 스플래시 유지
    private var startKey: NavKey? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { startKey == null }
        lockPortraitOnPhone()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        decideStartKey()
        setContent {
            RouteeTheme {
                startKey?.let { RouteeApp(startKey = it, sessionExpired = authSessionManager.sessionExpired) }
            }
        }
    }

    // 토큰이 없으면 로그인, 있으면 운동 메인 (진행 중인 기록은 운동 메인이 기록 화면으로 보냄)
    private fun decideStartKey() {
        lifecycleScope.launch {
            startKey = if (authSessionManager.hasSession()) WorkoutMainNavKey else LoginNavKey()
        }
    }
}
