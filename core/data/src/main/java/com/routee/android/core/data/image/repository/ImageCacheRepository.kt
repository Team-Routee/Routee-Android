package com.routee.android.core.data.image.repository

/** 서버 이미지(사진, 프로필) 캐시 */
interface ImageCacheRepository {
    /** 메모리, 디스크 캐시 전부 삭제 */
    suspend fun clear(): Result<Unit>
}
