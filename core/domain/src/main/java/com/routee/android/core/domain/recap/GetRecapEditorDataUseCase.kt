package com.routee.android.core.domain.recap

import com.routee.android.core.data.recap.repository.RecapRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * 기록 편집 화면 데이터 조회. 리캡 리소스와 경로를 동시에 불러온다
 *
 * 기록 편집 메인은 진입 전에 불러올 수 있는지 확인하고, 기록 편집 화면은 그리는 데 쓴다
 */
class GetRecapEditorDataUseCase @Inject constructor(
    private val recapRepository: RecapRepository,
) {
    suspend operator fun invoke(activityId: Long): Result<RecapEditorData> = coroutineScope {
        val resource = async { recapRepository.getRecapResource(activityId) }
        val track = async { recapRepository.getActivityTrack(activityId) }
        resource.await().fold(
            onSuccess = { recapResource ->
                track.await().map { RecapEditorData(resource = recapResource, track = it) }
            },
            onFailure = { Result.failure(it) },
        )
    }
}
