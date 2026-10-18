package com.routee.android.core.network.token

import com.routee.android.core.network.constant.AUTHORIZATION
import com.routee.android.core.network.model.BaseResponse
import com.routee.android.core.network.model.ReissueRequest
import com.routee.android.core.network.model.ReissueResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

internal interface ReissueService {
    @POST("api/v1/auth/reissue")
    suspend fun reissue(
        @Header(AUTHORIZATION) authorization: String,
        @Body request: ReissueRequest,
    ): BaseResponse<ReissueResponse>
}
