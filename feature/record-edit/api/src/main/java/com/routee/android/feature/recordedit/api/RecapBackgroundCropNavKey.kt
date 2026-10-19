package com.routee.android.feature.recordedit.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class RecapBackgroundCropNavKey(val imageUri: String) : NavKey
