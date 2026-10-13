package com.routee.android.core.data.auth.model

data class Agreements(
    val serviceTerms: Boolean,
    val privacyPolicy: Boolean,
    val locationServiceTerms: Boolean,
    val over14: Boolean,
    val marketingConsent: Boolean,
)
