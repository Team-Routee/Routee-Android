package com.routee.android.core.data.archive.repository

import com.routee.android.core.data.archive.model.ArchiveDetail

/** 활동 ID로 아카이브 상세 조회 */
interface ArchiveDetailRepository {
    suspend fun getDetail(activityId: Long): Result<ArchiveDetail>

    suspend fun updateTitle(activityId: Long, title: String): Result<Unit>

    suspend fun deleteActivity(activityId: Long): Result<Unit>
}
