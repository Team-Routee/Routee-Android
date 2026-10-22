package com.routee.android.feature.recordedit.complete

import android.graphics.Bitmap
import android.net.Uri

/**
 * 리캡 이미지 저장. 워터마크 합성, 앱 캐시 저장, 갤러리 저장, 공유 주소를 맡는다
 */
internal interface RecapImageRepository {
    /**
     * 오른쪽 아래에 워터마크를 합성해 캐시에 저장한다. 이전 리캡 이미지는 지운다
     *
     * @return 저장한 파일 경로
     */
    suspend fun saveWithWatermark(bitmap: Bitmap): Result<String>

    suspend fun saveToGallery(imagePath: String): Result<Unit>

    /**
     * 공유 시트에 넘길 content 주소
     */
    fun shareUri(imagePath: String): Uri
}
