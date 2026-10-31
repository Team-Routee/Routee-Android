package com.routee.android.feature.workout.recording

import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.domain.workout.AddWorkoutPhotoUseCase
import com.routee.android.core.domain.workout.GetUnfinishedWorkoutUseCase
import com.routee.android.core.domain.workout.SaveFinishedWorkoutUseCase
import com.routee.android.core.map.model.MapPoint
import com.routee.android.feature.workout.FakeLocationRepository
import com.routee.android.feature.workout.FakeMapImageRepository
import com.routee.android.feature.workout.FakeWorkoutPhotoRepository
import com.routee.android.feature.workout.FakeWorkoutRecordRepository
import com.routee.android.feature.workout.FakeWorkoutRemoteRepository
import com.routee.android.feature.workout.FakeWorkoutTrackingRepository
import com.routee.android.feature.workout.MainDispatcherRule
import com.routee.android.feature.workout.START
import com.routee.android.feature.workout.TestClock
import com.routee.android.feature.workout.api.WorkoutRecordingNavKey
import com.routee.android.feature.workout.photo
import com.routee.android.feature.workout.record
import com.routee.android.feature.workout.recording.model.OpenedPhoto
import com.routee.android.feature.workout.recording.model.WorkoutMode
import com.routee.android.feature.workout.recording.model.WorkoutRecordingSideEffect
import com.routee.android.feature.workout.recording.model.WorkoutSaveFailure
import com.routee.android.feature.workout.recording.model.WorkoutSummary
import com.routee.android.feature.workout.trackPoint
import java.io.File
import java.io.IOException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WorkoutRecordingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = TestClock()

    /**
     * GIVEN: 시작 API가 성공할 때
     * WHEN: 카운트다운이 끝나면
     * THEN: 서버가 준 id로 기기 기록을 시작하고 기록 중으로 바뀌며 위치 기록 서비스를 켠다
     */
    @Test
    fun `카운트다운이 끝나면 기록을 시작하고 서비스를 켠다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository()
        val viewModel = viewModel(tracking)

        viewModel.onCountdownFinish()

        assertEquals(1L, tracking.startedRecord?.activityId)
        assertEquals(WorkoutMode.RECORDING, viewModel.uiState.value.mode)
        assertEquals(WorkoutRecordingSideEffect.StartTrackingService, viewModel.sideEffect.first())
        viewModel.close()
    }

    /**
     * GIVEN: 시작 API가 실패할 때
     * WHEN: 카운트다운 중이면
     * THEN: 카운트다운 끝을 기다리지 않고 바로 시작 실패를 알리고, 카운트다운이 끝나도 기록을 시작하지 않는다
     */
    @Test
    fun `시작 API가 실패하면 카운트다운 중에 바로 알린다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository()
        val viewModel = viewModel(tracking, FakeWorkoutRemoteRepository(createError = IOException()))

        assertEquals(WorkoutRecordingSideEffect.StartFailed, viewModel.sideEffect.first())

        viewModel.onCountdownFinish()

        assertNull(tracking.startedRecord)
        assertEquals(WorkoutMode.COUNTDOWN, viewModel.uiState.value.mode)
        assertNull(withTimeoutOrNull(100) { viewModel.sideEffect.first() })
        viewModel.close()
    }

    /**
     * GIVEN: 기기 기록을 읽지 못할 때
     * WHEN: 화면에 들어와 카운트다운이 끝나면
     * THEN: 복원 실패를 알리고 새 기록을 시작한다
     */
    @Test
    fun `기기 기록 복원이 실패하면 알리고 새로 시작한다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository()
        val viewModel = viewModel(tracking, records = FakeWorkoutRecordRepository(getError = IOException()))

        assertEquals(WorkoutRecordingSideEffect.ShowRestoreFailed, viewModel.sideEffect.first())

        viewModel.onCountdownFinish()

        assertEquals(1L, tracking.startedRecord?.activityId)
        assertEquals(WorkoutMode.RECORDING, viewModel.uiState.value.mode)
        viewModel.close()
    }

    /**
     * GIVEN: 진행 중인 기록이 일시정지 상태로 남아 있을 때
     * WHEN: 화면에 들어오면
     * THEN: 카운트다운 없이 정지 화면으로 열고 서비스를 켠다
     */
    @Test
    fun `진행 중인 기록이 있으면 카운트다운 없이 이어간다`() = runTest {
        val viewModel = viewModel(FakeWorkoutTrackingRepository(record(WorkoutStatus.ACTIVITY_PAUSED)))

        assertEquals(WorkoutMode.PAUSED, viewModel.uiState.value.mode)
        assertEquals(WorkoutRecordingSideEffect.StartTrackingService, viewModel.sideEffect.first())
        viewModel.close()
    }

    /**
     * GIVEN: 앱이 꺼졌다 켜져 기록 화면이 바로 복원돼서 기기에만 기록이 있을 때
     * WHEN: 화면에 들어오고 카운트다운 끝 신호가 와도
     * THEN: 복구 모달을 띄우게 운동 메인으로 보내고 새 기록은 시작하지 않는다
     */
    @Test
    fun `앱이 꺼졌다 켜져 기록 화면이 바로 열리면 운동 메인으로 보낸다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository()
        val viewModel = viewModel(tracking, records = FakeWorkoutRecordRepository(record = record()))

        viewModel.onCountdownFinish()

        assertEquals(WorkoutRecordingSideEffect.NavigateToHome, viewModel.sideEffect.first())
        assertNull(tracking.startedRecord)
        viewModel.close()
    }

    /**
     * GIVEN: 복구 모달에서 이어서 기록을 골라 기록을 일시정지로 불러왔을 때
     * WHEN: 카운트다운이 끝나면
     * THEN: 새 기록 없이 불러온 기록을 기록 중으로 바꾸고 서비스를 켠다
     */
    @Test
    fun `복구로 들어오면 카운트다운 뒤 불러온 기록을 다시 시작한다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository(record().recovered(START))
        val viewModel = viewModel(tracking, key = WorkoutRecordingNavKey(isRecovering = true))
        assertEquals(WorkoutMode.COUNTDOWN, viewModel.uiState.value.mode)

        viewModel.onCountdownFinish()

        assertNull(tracking.startedRecord)
        assertEquals(WorkoutStatus.ACTIVITY_IN_PROGRESS, tracking.state.value.record?.status)
        assertEquals(WorkoutMode.RECORDING, viewModel.uiState.value.mode)
        assertEquals(WorkoutRecordingSideEffect.StartTrackingService, viewModel.sideEffect.first())
        viewModel.close()
    }

    /**
     * GIVEN: 복구로 들어왔는데 기기 저장이 실패할 때
     * WHEN: 카운트다운이 끝나 다시 시작하면
     * THEN: 기록이 일시정지 그대로라 정지 화면으로 둔다
     */
    @Test
    fun `복구한 기록을 다시 시작하지 못하면 정지 화면으로 둔다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository(record().recovered(START), resumeError = IOException())
        val viewModel = viewModel(tracking, key = WorkoutRecordingNavKey(isRecovering = true))

        viewModel.onCountdownFinish()

        assertEquals(WorkoutStatus.ACTIVITY_PAUSED, tracking.state.value.record?.status)
        assertEquals(WorkoutMode.PAUSED, viewModel.uiState.value.mode)
        assertEquals(WorkoutRecordingSideEffect.ShowResumeFailed, viewModel.sideEffect.first())
        viewModel.close()
    }

    /**
     * GIVEN: 정지 화면에서 기기 저장이 실패할 때
     * WHEN: 다시 시작을 누르면
     * THEN: 정지 화면에 남고 실패를 알린다
     */
    @Test
    fun `다시 시작하지 못하면 정지 화면에 남고 알린다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository(record(WorkoutStatus.ACTIVITY_PAUSED), resumeError = IOException())
        val viewModel = viewModel(tracking)
        assertEquals(WorkoutRecordingSideEffect.StartTrackingService, viewModel.sideEffect.first())

        viewModel.onRestartClick()

        assertEquals(WorkoutMode.PAUSED, viewModel.uiState.value.mode)
        assertEquals(WorkoutRecordingSideEffect.ShowResumeFailed, viewModel.sideEffect.first())
        viewModel.close()
    }

    /**
     * GIVEN: 앱이 다시 켜져 사진 목록이 비었고 서버에 올린 사진이 있을 때
     * WHEN: 복구로 들어오면
     * THEN: 서버 사진으로 마커와 개수를 채운다
     */
    @Test
    fun `복구로 들어오면 서버에 올린 사진으로 마커와 개수를 채운다`() = runTest {
        val serverPhoto = photo("timeline-9").copy(filePath = null, serverImageUrl = "https://image/9.jpg")
        val photos = FakeWorkoutPhotoRepository(serverPhotos = listOf(serverPhoto))
        val viewModel = viewModel(
            FakeWorkoutTrackingRepository(record().recovered(START)),
            photos = photos,
            key = WorkoutRecordingNavKey(isRecovering = true),
        )

        assertEquals(listOf(1L), photos.restoredActivityIds)
        assertEquals(1, viewModel.uiState.value.photoCount)
        assertEquals("https://image/9.jpg", viewModel.uiState.value.photos.single().imageUrl)
        viewModel.close()
    }

    /**
     * GIVEN: 기록 중일 때
     * WHEN: 저장소에 경로 좌표가 쌓이고 5분이 지나면
     * THEN: 거리, 최고 고도는 반올림하고 시간은 초 단위로, 경로는 좌표 목록으로 보여준다
     */
    @Test
    fun `저장소 상태를 거리, 시간, 고도, 경로로 보여준다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository(record())
        val viewModel = viewModel(tracking)
        clock.now = START.plusSeconds(300)

        tracking.setState(WorkoutTrackingState(record = record(), trackPoints = listOf(trackPoint(1), trackPoint(2))))

        val state = viewModel.uiState.value
        assertEquals(1_234, state.distanceMeters)
        assertEquals(300, state.durationSec)
        assertEquals(303, state.maxElevationMeters)
        assertEquals(listOf(MapPoint(38.0, 127.0), MapPoint(39.0, 127.0)), state.route)
        viewModel.close()
    }

    /**
     * GIVEN: 기록 중일 때
     * WHEN: 정지를 누르면
     * THEN: 저장소 상태를 따라 정지 화면으로 바뀐다
     */
    @Test
    fun `정지를 누르면 저장소 상태를 따라 정지 화면이 된다`() = runTest {
        val viewModel = viewModel(FakeWorkoutTrackingRepository(record()))

        viewModel.onStopClick()

        assertEquals(WorkoutMode.PAUSED, viewModel.uiState.value.mode)
        viewModel.close()
    }

    /**
     * GIVEN: 경로 좌표가 2개인 일시정지 기록일 때
     * WHEN: 종료를 길게 누르면
     * THEN: 마무리 화면으로 바뀌고 기록을 업로드 대기로 바꾼 뒤 경로로 지도 캡처를 요청한다
     */
    @Test
    fun `종료하면 업로드 대기로 바꾸고 지도 캡처를 요청한다`() = runTest {
        val tracking = pausedWithPoints(2)
        val viewModel = viewModel(tracking)
        viewModel.sideEffect.first()

        viewModel.onEndLongPress()

        assertEquals(WorkoutMode.FINISHING, viewModel.uiState.value.mode)
        assertEquals(WorkoutStatus.PENDING_UPLOAD, tracking.state.value.record?.status)
        assertEquals(
            listOf(MapPoint(38.0, 127.0), MapPoint(39.0, 127.0)),
            viewModel.uiState.value.captureRoute,
        )
        viewModel.close()
    }

    /**
     * GIVEN: 지도 캡처를 요청한 뒤 화면이 다시 만들어져 캡처가 두 번 올 때
     * WHEN: 캡처를 받으면
     * THEN: 캡처 요청을 비우고 처음 온 캡처로 한 번만 저장한다
     */
    @Test
    fun `캡처는 한 번만 저장한다`() = runTest {
        val tracking = pausedWithPoints(2)
        val viewModel = viewModel(tracking)
        viewModel.onEndLongPress()

        viewModel.onMapCaptured(byteArrayOf(1))
        viewModel.onMapCaptured(null)
        viewModel.onFinishAnimationEnd()

        assertNull(viewModel.uiState.value.captureRoute)
        assertTrue(
            withTimeout(EFFECT_TIMEOUT_MILLIS) {
                viewModel.sideEffect.dropWhile { it !is WorkoutRecordingSideEffect.NavigateToTimeline }.first()
            } is WorkoutRecordingSideEffect.NavigateToTimeline,
        )
        viewModel.close()
    }

    /**
     * GIVEN: 종료 후 지도 캡처가 왔을 때
     * WHEN: 마무리 애니메이션이 끝나면
     * THEN: 저장을 마치고 기록 요약을 들고 타임라인으로 간다
     */
    @Test
    fun `저장되면 마무리 애니메이션 뒤 타임라인으로 간다`() = runTest {
        val tracking = pausedWithPoints(2)
        val viewModel = viewModel(tracking)
        viewModel.onEndLongPress()

        viewModel.onMapCaptured(byteArrayOf(1))
        viewModel.onFinishAnimationEnd()

        val summary = WorkoutSummary(
            activityId = 1,
            endedAtEpochMillis = START.toEpochMilli(),
            distanceMeters = 1_234,
            durationSeconds = 60,
            maxElevationMeters = 303,
        )
        assertEquals(
            WorkoutRecordingSideEffect.NavigateToTimeline(summary),
            withTimeout(EFFECT_TIMEOUT_MILLIS) {
                viewModel.sideEffect.dropWhile { it !is WorkoutRecordingSideEffect.NavigateToTimeline }.first()
            },
        )
        assertTrue(tracking.isCleared)
        viewModel.close()
    }

    /**
     * GIVEN: 경로 좌표가 1개뿐인 기록일 때
     * WHEN: 종료 후 마무리 애니메이션이 끝나면
     * THEN: 기록 등록 실패 모달을 띄우고, 확인을 누르면 홈으로 간다
     */
    @Test
    fun `경로 좌표가 부족하면 기록 등록 실패 모달을 띄운다`() = runTest {
        val viewModel = viewModel(pausedWithPoints(1))
        viewModel.onEndLongPress()
        viewModel.onMapCaptured(byteArrayOf(1))

        viewModel.onFinishAnimationEnd()
        assertEquals(WorkoutSaveFailure.INVALID_ROUTE, viewModel.uiState.value.saveFailure)

        viewModel.onSaveFailureDialogConfirm()
        assertEquals(
            WorkoutRecordingSideEffect.NavigateToHome,
            withTimeout(EFFECT_TIMEOUT_MILLIS) {
                viewModel.sideEffect.dropWhile {
                    it !=
                        WorkoutRecordingSideEffect.NavigateToHome
                }.first()
            },
        )
        viewModel.close()
    }

    /**
     * GIVEN: 지도 캡처에 실패했을 때
     * WHEN: 마무리 애니메이션이 끝나면
     * THEN: 기록 등록 실패 모달을 띄우고 기기 기록은 남긴다
     */
    @Test
    fun `캡처에 실패하면 기록 등록 실패 모달을 띄운다`() = runTest {
        val tracking = pausedWithPoints(2)
        val viewModel = viewModel(tracking)
        viewModel.onEndLongPress()
        viewModel.onMapCaptured(null)

        viewModel.onFinishAnimationEnd()

        assertEquals(WorkoutSaveFailure.NETWORK, viewModel.uiState.value.saveFailure)
        assertFalse(tracking.isCleared)
        viewModel.close()
    }

    /**
     * GIVEN: 기기에 마지막 위치가 있을 때
     * WHEN: 기록 화면에 들어오면
     * THEN: 그 위치에서 지도를 시작한다
     */
    @Test
    fun `마지막 위치가 있으면 그 위치에서 지도를 시작한다`() = runTest {
        val lastLocation = DeviceLocation(
            latitude = 37.0,
            longitude = 127.0,
            altitude = null,
            altitudeAccuracyMeters = null,
            accuracyMeters = 5f,
            time = START,
        )

        val viewModel =
            viewModel(FakeWorkoutTrackingRepository(), location = FakeLocationRepository(lastLocation = lastLocation))

        assertEquals(WorkoutRecordingSideEffect.MoveToLastLocation(MapPoint(37.0, 127.0)), viewModel.sideEffect.first())
        viewModel.close()
    }

    /**
     * GIVEN: 기록 중일 때
     * WHEN: 카메라 버튼을 두 번 빠르게 누르면
     * THEN: 촬영 파일을 한 번만 만들어 카메라를 한 번만 연다
     */
    @Test
    fun `카메라를 두 번 눌러도 한 번만 연다`() = runTest {
        val photos = FakeWorkoutPhotoRepository()
        val viewModel = viewModel(FakeWorkoutTrackingRepository(record()), photos = photos)
        viewModel.sideEffect.first()

        viewModel.onCameraClick()
        viewModel.onCameraClick()

        assertEquals(WorkoutRecordingSideEffect.TakePhoto("capture-1.jpg"), viewModel.sideEffect.first())
        assertEquals(1, photos.captureFileCount)
        viewModel.close()
    }

    /**
     * GIVEN: 경로 좌표가 있는 기록 중일 때
     * WHEN: 사진을 찍으면
     * THEN: 사진 개수와 지도 마커에 마지막 경로 좌표로 반영한다
     */
    @Test
    fun `사진을 찍으면 개수와 마커가 늘어난다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository(record()).apply {
            setState(WorkoutTrackingState(record = record(), trackPoints = listOf(trackPoint(1), trackPoint(2))))
        }
        val viewModel = viewModel(tracking)

        viewModel.onPhotoTaken("capture-1.jpg", isSaved = true)

        val state = viewModel.uiState.value
        assertEquals(1, state.photoCount)
        assertEquals(MapPoint(39.0, 127.0), state.photos.single().point)
        assertEquals(File("capture-1.jpg").toURI().toString(), state.photos.single().imageUrl)
        viewModel.close()
    }

    /**
     * GIVEN: 카메라를 연 뒤
     * WHEN: 찍지 않고 돌아오면
     * THEN: 카메라 원본 파일을 지우고 사진은 늘지 않는다
     */
    @Test
    fun `촬영을 취소하면 원본 파일을 지운다`() = runTest {
        val photos = FakeWorkoutPhotoRepository()
        val viewModel = viewModel(FakeWorkoutTrackingRepository(record()), photos = photos)

        viewModel.onPhotoTaken("capture-1.jpg", isSaved = false)

        assertEquals(listOf(File("capture-1.jpg")), photos.discarded)
        assertEquals(0, viewModel.uiState.value.photoCount)
        viewModel.close()
    }

    /**
     * GIVEN: 기본 카메라 앱이 없을 때
     * WHEN: 카메라를 열지 못하면
     * THEN: 원본 파일을 지우고 안내 모달을 띄우고, 확인을 누르면 닫는다
     */
    @Test
    fun `카메라 앱이 없으면 안내 모달을 띄운다`() = runTest {
        val photos = FakeWorkoutPhotoRepository()
        val viewModel = viewModel(FakeWorkoutTrackingRepository(record()), photos = photos)

        viewModel.onCameraUnavailable("capture-1.jpg")
        assertTrue(viewModel.uiState.value.isCameraUnavailable)
        assertEquals(listOf(File("capture-1.jpg")), photos.discarded)

        viewModel.onCameraUnavailableDialogConfirm()
        assertFalse(viewModel.uiState.value.isCameraUnavailable)
        viewModel.close()
    }

    /**
     * GIVEN: 종료 때 다시 올려도 사진 1장이 실패할 때
     * WHEN: 마무리 애니메이션이 끝나면
     * THEN: 빠진 사진을 알리고 타임라인으로 간다
     */
    @Test
    fun `빠진 사진이 있으면 알리고 타임라인으로 간다`() = runTest {
        val viewModel = viewModel(pausedWithPoints(2), photos = FakeWorkoutPhotoRepository(missingCount = 1))
        viewModel.onEndLongPress()

        viewModel.onMapCaptured(byteArrayOf(1))
        viewModel.onFinishAnimationEnd()

        withTimeout(EFFECT_TIMEOUT_MILLIS) {
            viewModel.sideEffect.dropWhile { it != WorkoutRecordingSideEffect.ShowPhotosMissing }.first()
        }
        assertTrue(viewModel.sideEffect.first() is WorkoutRecordingSideEffect.NavigateToTimeline)
        viewModel.close()
    }

    /**
     * GIVEN: 사진이 있는 기록 중일 때
     * WHEN: 사진 마커를 탭하고 장소명을 바꿔 닫으면
     * THEN: 모달을 열었다 닫고 바뀐 장소명을 저장한다
     */
    @Test
    fun `모달을 닫을 때 바뀐 장소명을 저장한다`() = runTest {
        val photos = FakeWorkoutPhotoRepository().apply { setPhotos(listOf(photo("a"))) }
        val viewModel = viewModel(FakeWorkoutTrackingRepository(record()), photos = photos)

        viewModel.onPhotoMarkerClick("a")
        assertEquals(
            OpenedPhoto(id = "a", imageUrl = File("a.jpg").toURI().toString(), title = "말바위"),
            viewModel.uiState.value.openedPhoto,
        )

        viewModel.onPhotoDialogClose("말바위 전망대")
        assertNull(viewModel.uiState.value.openedPhoto)
        assertEquals("말바위 전망대", photos.photos.value.single().title)
        viewModel.close()
    }

    /**
     * GIVEN: 사진이 있는 기록 중일 때
     * WHEN: 장소명을 공백만 남기고 닫으면
     * THEN: 이전 장소명을 유지한다
     */
    @Test
    fun `장소명을 비우고 닫으면 이전 장소명을 유지한다`() = runTest {
        val photos = FakeWorkoutPhotoRepository().apply { setPhotos(listOf(photo("a"))) }
        val viewModel = viewModel(FakeWorkoutTrackingRepository(record()), photos = photos)
        viewModel.onPhotoMarkerClick("a")

        viewModel.onPhotoDialogClose("  ")

        assertNull(viewModel.uiState.value.openedPhoto)
        assertEquals("말바위", photos.photos.value.single().title)
        viewModel.close()
    }

    /**
     * GIVEN: 사진 2장이 있는 기록 중일 때
     * WHEN: 모달에서 삭제하고 스낵바가 시간이 지나 닫히면
     * THEN: 지도와 개수에서 바로 빼고, 스낵바가 닫히면 삭제를 확정한다
     */
    @Test
    fun `삭제하면 바로 빼고 스낵바가 닫히면 확정한다`() = runTest {
        val photos = FakeWorkoutPhotoRepository().apply { setPhotos(listOf(photo("a"), photo("b"))) }
        val viewModel = viewModel(FakeWorkoutTrackingRepository(record()), photos = photos)
        viewModel.onPhotoMarkerClick("a")

        viewModel.onPhotoDeleteClick()

        assertEquals(1, viewModel.uiState.value.photoCount)
        assertTrue(viewModel.uiState.value.isPhotoDeletePending)
        assertEquals(2, photos.photos.value.size)
        assertEquals(
            WorkoutRecordingSideEffect.ShowPhotoDeleted("a"),
            withTimeout(EFFECT_TIMEOUT_MILLIS) {
                viewModel.sideEffect.dropWhile { it !is WorkoutRecordingSideEffect.ShowPhotoDeleted }.first()
            },
        )

        viewModel.onPhotoDeleteSnackbarClose("a", isUndone = false)

        assertEquals(listOf("b"), photos.photos.value.map { it.id })
        assertFalse(viewModel.uiState.value.isPhotoDeletePending)
        viewModel.close()
    }

    /**
     * GIVEN: 사진을 삭제해 스낵바가 떠 있을 때
     * WHEN: [취소]를 누르면
     * THEN: 사진과 개수를 되돌린다
     */
    @Test
    fun `삭제를 취소하면 사진을 되돌린다`() = runTest {
        val photos = FakeWorkoutPhotoRepository().apply { setPhotos(listOf(photo("a"))) }
        val viewModel = viewModel(FakeWorkoutTrackingRepository(record()), photos = photos)
        viewModel.onPhotoMarkerClick("a")
        viewModel.onPhotoDeleteClick()

        viewModel.onPhotoDeleteSnackbarClose("a", isUndone = true)

        assertEquals(1, viewModel.uiState.value.photoCount)
        assertEquals(1, photos.photos.value.size)
        viewModel.close()
    }

    /**
     * GIVEN: 사진을 삭제해 스낵바가 떠 있을 때
     * WHEN: 카메라 버튼을 누르면
     * THEN: 카메라를 열지 않는다
     */
    @Test
    fun `삭제 스낵바가 떠 있으면 카메라를 열지 않는다`() = runTest {
        val photos = FakeWorkoutPhotoRepository().apply { setPhotos(listOf(photo("a"))) }
        val viewModel = viewModel(FakeWorkoutTrackingRepository(record()), photos = photos)
        viewModel.onPhotoMarkerClick("a")
        viewModel.onPhotoDeleteClick()

        viewModel.onCameraClick()

        assertEquals(0, photos.captureFileCount)
        viewModel.close()
    }

    /** 1초마다 시간을 갱신하는 코루틴을 멈춤, 안 멈추면 runTest가 가상 시간을 끝없이 돌림 */
    private fun WorkoutRecordingViewModel.close() = viewModelScope.cancel()

    private fun viewModel(
        tracking: FakeWorkoutTrackingRepository,
        remote: FakeWorkoutRemoteRepository = FakeWorkoutRemoteRepository(),
        location: FakeLocationRepository = FakeLocationRepository(),
        photos: FakeWorkoutPhotoRepository = FakeWorkoutPhotoRepository(),
        records: FakeWorkoutRecordRepository = FakeWorkoutRecordRepository(),
        key: WorkoutRecordingNavKey = WorkoutRecordingNavKey(),
    ) = WorkoutRecordingViewModel(
        key = key,
        trackingRepository = tracking,
        getUnfinishedWorkout = GetUnfinishedWorkoutUseCase(tracking, records, clock),
        remoteRepository = remote,
        saveFinishedWorkout = SaveFinishedWorkoutUseCase(
            trackingRepository = tracking,
            recordRepository = records,
            mapImageRepository = FakeMapImageRepository(),
            remoteRepository = remote,
            photoRepository = photos,
        ),
        addWorkoutPhoto = AddWorkoutPhotoUseCase(
            trackingRepository = tracking,
            photoRepository = photos,
            locationRepository = location,
            clock = clock,
        ),
        photoRepository = photos,
        locationRepository = location,
        clock = clock,
    )

    private fun pausedWithPoints(count: Int) = FakeWorkoutTrackingRepository(
        record(WorkoutStatus.ACTIVITY_PAUSED),
    ).apply {
        setState(
            WorkoutTrackingState(
                record = record(WorkoutStatus.ACTIVITY_PAUSED).copy(durationMillis = 60_000),
                trackPoints = (1..count).map(::trackPoint),
            ),
        )
    }

    private companion object {
        const val EFFECT_TIMEOUT_MILLIS = 5_000L
    }
}
