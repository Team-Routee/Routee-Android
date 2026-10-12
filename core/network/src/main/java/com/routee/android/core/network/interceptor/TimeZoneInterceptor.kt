package com.routee.android.core.network.interceptor

import com.routee.android.core.network.constant.TIME_ZONE
import java.util.TimeZone
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 기기 시간대(예: Asia/Seoul)를 Time-Zone 헤더에 첨부한다
 *
 * 월별 기록 조회처럼 날짜 경계를 사용자 시간대로 계산하는 API에서 사용한다
 */
internal class TimeZoneInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(TIME_ZONE, TimeZone.getDefault().id)
            .build()

        return chain.proceed(request)
    }
}
