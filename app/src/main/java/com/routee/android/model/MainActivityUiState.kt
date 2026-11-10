package com.routee.android.model

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.data.appnotice.model.AppNotice

@Immutable
internal data class MainActivityUiState(
    val startKey: NavKey? = null, // null 이면 확인 중, 그동안 스플래시 유지
    val appNotice: AppNotice? = null,
)
