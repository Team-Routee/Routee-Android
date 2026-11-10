package com.routee.android

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.link.RouteeLink
import com.routee.android.core.ui.link.tryOpenUri
import com.routee.android.ui.AppNoticeDialog
import com.routee.android.ui.RouteeApp
import com.routee.android.util.lockPortraitOnPhone
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value.startKey == null }
        lockPortraitOnPhone()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val uriHandler = LocalUriHandler.current
            RouteeTheme {
                if (uiState.appNotice?.blocksApp != true) {
                    uiState.startKey?.let { RouteeApp(startKey = it, sessionExpired = viewModel.sessionExpired) }
                }
                uiState.appNotice?.let { notice ->
                    AppNoticeDialog(
                        notice = notice,
                        onUpdateClick = {
                            uriHandler.tryOpenUri(RouteeLink.PLAY_STORE_APP.url) ||
                                uriHandler.tryOpenUri(RouteeLink.PLAY_STORE.url)
                        },
                        onDismissRequest = viewModel::dismissAppNotice,
                        onExitRequest = ::finish,
                    )
                }
            }
        }
    }
}
