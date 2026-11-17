package com.routee.android

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import timber.log.Timber

// INFO 이상 로그를 크래시 리포트 Logs에 남김, 스택은 첫 줄만
internal class CrashlyticsTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= Log.INFO

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val error = t?.let { " (${it::class.simpleName})" }.orEmpty()
        Firebase.crashlytics.log("${PRIORITY_LABELS[priority]}/${message.substringBefore('\n')}$error")
    }

    private companion object {
        val PRIORITY_LABELS = mapOf(Log.INFO to "I", Log.WARN to "W", Log.ERROR to "E", Log.ASSERT to "A")
    }
}
