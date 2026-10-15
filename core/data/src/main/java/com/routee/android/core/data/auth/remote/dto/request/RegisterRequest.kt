package com.routee.android.core.data.auth.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RegisterRequest(
    @SerialName("nickname")
    val nickname: String,
    @SerialName("idToken")
    val idToken: String,
    @SerialName("provider")
    val provider: String,
    @SerialName("agreements")
    val agreements: AgreementsRequest,
)

@Serializable
internal data class AgreementsRequest(
    @SerialName("serviceTerms")
    val serviceTerms: Boolean,
    @SerialName("privacyPolicy")
    val privacyPolicy: Boolean,
    @SerialName("locationServiceTerms")
    val locationServiceTerms: Boolean,
    @SerialName("over14")
    val over14: Boolean,
    @SerialName("marketingConsent")
    val marketingConsent: Boolean,
)
