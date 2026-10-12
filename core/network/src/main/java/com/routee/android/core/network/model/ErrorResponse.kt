package com.routee.android.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 실패 응답의 에러 바디. 공통 응답 형식에서 data를 제외한 값만 읽는다
 */
@Serializable
internal data class ErrorResponse(
    @SerialName("status")
    val status: Int,
    @SerialName("code")
    val code: String,
    @SerialName("message")
    val message: String,
)
