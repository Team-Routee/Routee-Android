package com.routee.android.core.domain.recap

import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.recap.model.RecapResource

/**
 * 기록 편집 화면을 그리는 데 필요한 데이터
 *
 * @param resource 운동 수치, 지도, 루트
 * @param track    경로와 사진 위치
 */
data class RecapEditorData(
    val resource: RecapResource,
    val track: ActivityTrack,
)
