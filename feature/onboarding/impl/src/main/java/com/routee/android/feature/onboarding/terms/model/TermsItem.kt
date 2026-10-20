package com.routee.android.feature.onboarding.terms.model

import androidx.annotation.StringRes
import com.routee.android.core.ui.link.RouteeLink
import com.routee.android.feature.onboarding.R

internal enum class TermsItem(
    @param:StringRes val titleRes: Int,
    val isRequired: Boolean,
    val document: RouteeLink?,
) {
    SERVICE(R.string.onboarding_terms_item_service, isRequired = true, document = RouteeLink.SERVICE_TERMS),
    PRIVACY(R.string.onboarding_terms_item_privacy, isRequired = true, document = RouteeLink.PRIVACY_POLICY),
    LOCATION(R.string.onboarding_terms_item_location, isRequired = true, document = RouteeLink.LOCATION_TERMS),
    OVER_14(R.string.onboarding_terms_item_over_14, isRequired = true, document = null),
    MARKETING(R.string.onboarding_terms_item_marketing, isRequired = false, document = null),
}
