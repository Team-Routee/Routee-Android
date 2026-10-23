package com.routee.android.core.network.session

import com.routee.android.core.network.token.TokenProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 로그인 세션 관리자
 *
 * 토큰 재발급과 삭제는 같은 Mutex 안에서 차례로 실행된다. 재발급까지 실패하면 [sessionExpired]로 알린다
 */
@Singleton
class AuthSessionManager @Inject constructor(
    private val tokenProvider: TokenProvider,
) {
    private val mutex = Mutex()
    private val _sessionExpired = Channel<Unit>(Channel.CONFLATED)

    /**
     * 세션 만료 이벤트. 받으면 로그인 화면으로 이동한다
     */
    val sessionExpired: Flow<Unit> = _sessionExpired.receiveAsFlow()

    /**
     * 저장된 로그인 세션이 있는지. 앱 시작 화면(로그인, 홈)을 정할 때 사용
     */
    suspend fun hasSession(): Boolean = mutex.withLock { tokenProvider.getAccessToken() != null }

    /**
     * 저장된 토큰 삭제. 로그아웃처럼 사용자가 직접 세션을 끝낼 때 쓰며 [sessionExpired]는 보내지 않는다
     */
    suspend fun clearSession() {
        mutex.withLock { tokenProvider.clearTokens() }
    }

    internal suspend fun <T> withTokenLock(block: suspend TokenProvider.() -> T): T =
        mutex.withLock { tokenProvider.block() }

    internal fun notifySessionExpired() {
        _sessionExpired.trySend(Unit)
    }
}
