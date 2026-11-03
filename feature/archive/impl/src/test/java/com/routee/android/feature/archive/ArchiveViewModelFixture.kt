package com.routee.android.feature.archive

import androidx.lifecycle.SavedStateHandle
import com.routee.android.core.data.archive.repository.ArchiveRepository
import com.routee.android.core.data.connectivity.repository.ConnectivityRepository
import com.routee.android.core.data.member.repository.MemberRepository
import kotlinx.coroutines.flow.flowOf

internal fun archiveViewModel(
    repository: ArchiveRepository,
    members: MemberRepository,
    savedStateHandle: SavedStateHandle,
) = ArchiveViewModel(
    repository,
    members,
    savedStateHandle,
    object : ConnectivityRepository {
        override suspend fun isConnected() = true
        override fun observeConnection() = flowOf(true)
    },
)
