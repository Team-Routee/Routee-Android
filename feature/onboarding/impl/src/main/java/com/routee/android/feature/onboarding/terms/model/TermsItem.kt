package com.routee.android.feature.onboarding.terms.model

import androidx.annotation.StringRes
import com.routee.android.feature.onboarding.R

internal enum class TermsItem(
    @param:StringRes val titleRes: Int,
    val isRequired: Boolean,
    val document: TermsDocument?,
) {
    SERVICE(R.string.onboarding_terms_item_service, isRequired = true, document = TermsDocument.SERVICE),
    PRIVACY(R.string.onboarding_terms_item_privacy, isRequired = true, document = TermsDocument.PRIVACY),
    LOCATION(R.string.onboarding_terms_item_location, isRequired = true, document = TermsDocument.LOCATION),
    OVER_14(R.string.onboarding_terms_item_over_14, isRequired = true, document = null),
    MARKETING(R.string.onboarding_terms_item_marketing, isRequired = false, document = null),
}
