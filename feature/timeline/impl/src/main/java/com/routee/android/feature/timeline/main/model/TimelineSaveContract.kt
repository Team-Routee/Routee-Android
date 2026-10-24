package com.routee.android.feature.timeline.main.model

import com.routee.android.core.data.recap.exception.RecapException
import com.routee.android.core.network.exception.ApiException

internal enum class TimelineRequestResult { Success, NetworkFailed, Failed }

internal enum class TimelineSaveDestination { Home, RecordEdit }

internal fun Throwable.toRequestFailure(): TimelineRequestResult =
    if (this is ApiException.NetworkConnection || this is RecapException.NetworkUnavailable) {
        TimelineRequestResult.NetworkFailed
    } else {
        TimelineRequestResult.Failed
    }
