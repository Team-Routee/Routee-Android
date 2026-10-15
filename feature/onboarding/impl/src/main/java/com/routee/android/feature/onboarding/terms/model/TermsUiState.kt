package com.routee.android.feature.onboarding.terms.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class TermsUiState(
    val checkedItems: Set<TermsItem> = emptySet(),
) {
    val isAllChecked: Boolean get() = checkedItems.size == TermsItem.entries.size

    val isNextEnabled: Boolean get() = TermsItem.entries.filter { it.isRequired }.all { it in checkedItems }

    fun isChecked(item: TermsItem): Boolean = item in checkedItems
}
