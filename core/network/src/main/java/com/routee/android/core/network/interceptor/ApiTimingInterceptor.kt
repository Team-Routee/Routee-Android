package com.routee.android.core.network.interceptor

import java.io.IOException
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

/**
 * API 경로, 응답 코드, 소요 시간을 남긴다
 *
 * 쿼리는 남기지 않는다
 */
internal class ApiTimingInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val api = "${request.method} ${request.url.encodedPath}"
        val startedAt = System.nanoTime()
        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            Timber.i("API $api 실패 ${elapsedMillis(startedAt)}ms (${e::class.simpleName})")
            throw e
        }
        Timber.i("API $api ${response.code} ${elapsedMillis(startedAt)}ms")
        return response
    }

    private fun elapsedMillis(startedAt: Long): Long = (System.nanoTime() - startedAt) / 1_000_000
}
