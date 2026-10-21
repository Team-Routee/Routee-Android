package com.routee.android.core.data.member.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemberSummaryResponse(
    @SerialName("nickname")
    val nickname: String,
    @SerialName("profileImageUrl")
    val profileImageUrl: String? = null,
    @SerialName("joinDate")
    val joinDate: String,
    @SerialName("daysSinceJoining")
    val daysSinceJoining: Int,
    @SerialName("totalActivityCount")
    val totalActivityCount: Int,
)
