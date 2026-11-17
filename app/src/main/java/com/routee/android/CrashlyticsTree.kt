package com.routee.android

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.routee.android.core.network.exception.ApiException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import timber.log.Timber

// INFO 이상 로그를 크래시 리포트 Logs에 남김, 예상 못 한 ERROR 예외는 non-fatal로 전송
internal class CrashlyticsTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= Log.INFO

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val error = t?.let { " (${it::class.simpleName})" }.orEmpty()
        Firebase.crashlytics.log("${PRIORITY_LABELS[priority]}/${message.substringBefore('\n')}$error")
        if (priority >= Log.ERROR && t != null && !t.isExpected()) {
            Firebase.crashlytics.recordException(t)
        }
    }

    // 앱이 이미 처리하는 오류
    private fun Throwable.isExpected(): Boolean =
        this is ApiException || this is IOException || this is CancellationException

    private companion object {
        val PRIORITY_LABELS = mapOf(Log.INFO to "I", Log.WARN to "W", Log.ERROR to "E", Log.ASSERT to "A")
    }
}
