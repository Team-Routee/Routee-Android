package com.routee.android.feature.recordedit.model

/**
 * 기록 카드 썸네일 모양 (피그마 workout_recap_item의 photoCount)
 *
 * 큰 사진 1장 위에 작은 사진을 최대 3장까지 겹쳐 보여준다
 */
internal enum class RecapThumbnailType(val smallPhotoCount: Int) {
    NONE(smallPhotoCount = 0),
    SINGLE(smallPhotoCount = 0),
    DOUBLE(smallPhotoCount = 1),
    TRIPLE(smallPhotoCount = 2),
    MORE(smallPhotoCount = 3),
    ;

    companion object {
        fun from(photoCount: Int): RecapThumbnailType = when {
            photoCount <= 0 -> NONE
            photoCount == 1 -> SINGLE
            photoCount == 2 -> DOUBLE
            photoCount == 3 -> TRIPLE
            else -> MORE
        }
    }
}
