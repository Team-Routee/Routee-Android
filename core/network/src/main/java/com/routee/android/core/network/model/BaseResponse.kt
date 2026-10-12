package com.routee.android.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param status HTTP 상태 코드
 * @param code 응답 구분 상세 코드 (예: ACTIVITY_RECAP_GET_SUCCESS)
 * @param message 응답 상세 내용
 * @param data 응답 데이터. 실패 응답이나 데이터가 없는 응답에서는 null
 */
@Serializable
data class BaseResponse<T>(
    @SerialName("status")
    val status: Int,
    @SerialName("code")
    val code: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: T? = null,
)
