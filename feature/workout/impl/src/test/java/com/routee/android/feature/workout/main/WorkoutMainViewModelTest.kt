package com.routee.android.feature.workout.main

import androidx.lifecycle.SavedStateHandle
import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.workout.model.WORKOUT_PAUSE_LIMIT
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.domain.location.ObserveCurrentAddressUseCase
import com.routee.android.core.domain.workout.DiscardWorkoutUseCase
import com.routee.android.core.domain.workout.GetUnfinishedWorkoutUseCase
import com.routee.android.core.domain.workout.SaveFinishedWorkoutUseCase
import com.routee.android.core.map.model.MapPoint
import com.routee.android.feature.workout.FakeLocationRepository
import com.routee.android.feature.workout.FakeMapImageRepository
import com.routee.android.feature.workout.FakePermissionRepository
import com.routee.android.feature.workout.FakeReverseGeocodingRepository
import com.routee.android.feature.workout.FakeWorkoutPhotoRepository
import com.routee.android.feature.workout.FakeWorkoutRecordRepository
import com.routee.android.feature.workout.FakeWorkoutRemoteRepository
import com.routee.android.feature.workout.FakeWorkoutTrackingRepository
import com.routee.android.feature.workout.MainDispatcherRule
import com.routee.android.feature.workout.START
import com.routee.android.feature.workout.TestClock
import com.routee.android.feature.workout.main.model.LocationPermission
import com.routee.android.feature.workout.main.model.WorkoutMainSideEffect
import com.routee.android.feature.workout.main.model.WorkoutRecoveryUiModel
import com.routee.android.feature.workout.record
import com.routee.android.feature.workout.trackPoint
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WorkoutMainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val permissions = FakePermissionRepository()
    private val clock = TestClock()

    /**
     * GIVEN: 앱이 살아 있고 기록 중일 때
     * WHEN: 운동 메인에 들어오면
     * THEN: 안내 없이 기록 화면으로 보낸다
     */
    @Test
    fun `앱이 살아 있고 기록 중이면 기록 화면으로 보낸다`() = runTest {
        val viewModel = viewModel(FakeWorkoutTrackingRepository(record()))

        assertEquals(WorkoutMainSideEffect.NavigateToRecording, viewModel.sideEffect.first())
        assertNull(viewModel.uiState.value.recovery)
    }

    /**
     * GIVEN: 진행 중인 기록이 없을 때
     * WHEN: 운동 메인에 들어오면
     * THEN: 홈에 머문다
     */
    @Test
    fun `진행 중인 기록이 없으면 홈에 머문다`() = runTest {
        val viewModel = viewModel()

        assertNull(withTimeoutOrNull(100) { viewModel.sideEffect.first() })
        assertNull(viewModel.uiState.value.recovery)
    }

    /**
     * GIVEN: 앱이 꺼졌다 켜져서 기기에만 기록 중인 기록이 남았을 때
     * WHEN: 운동 메인에 들어오면
     * THEN: 기록 화면으로 보내지 않고 마지막 기록 시각, 거리, 시간을 담은 복구 모달을 띄운다
     */
    @Test
    fun `앱이 꺼졌다 켜지면 복구 모달을 띄운다`() = runTest {
        val viewModel = viewModel(records = FakeWorkoutRecordRepository(STOPPED_RECORD))

        assertEquals(
            WorkoutRecoveryUiModel(lastRecordedAt = START.plusSeconds(60), distanceMeters = 1_234, durationSec = 60),
            viewModel.uiState.value.recovery,
        )
        assertNull(withTimeoutOrNull(100) { viewModel.sideEffect.first() })
    }

    /**
     * GIVEN: 앱이 살아 있고 일시정지한 지 2시간이 지났을 때
     * WHEN: 운동 메인에 들어오면
     * THEN: 기록 화면 대신 복구 모달을 띄운다
     */
    @Test
    fun `일시정지 2시간이 지나면 복구 모달을 띄운다`() = runTest {
        clock.now = START.plus(WORKOUT_PAUSE_LIMIT)

        val viewModel = viewModel(FakeWorkoutTrackingRepository(record().paused(START)))

        assertNotNull(viewModel.uiState.value.recovery)
        assertNull(withTimeoutOrNull(100) { viewModel.sideEffect.first() })
    }

    /**
     * GIVEN: 복구 모달이 떠 있고 위치 권한이 있을 때
     * WHEN: 이어서 기록을 누르고 알림 권한 결과가 오면
     * THEN: 기록을 일시정지로 불러오고 복구한 기록 화면으로 보낸다
     */
    @Test
    fun `이어서 기록은 권한 흐름 뒤 기록을 불러와 기록 화면으로 보낸다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository(savedRecord = STOPPED_RECORD)
        val viewModel = grantedViewModel(tracking, FakeWorkoutRecordRepository(STOPPED_RECORD))

        viewModel.onRecoveryContinueClick()
        assertEquals(WorkoutMainSideEffect.RequestNotificationPermission, viewModel.sideEffect.first())
        viewModel.onNotificationPermissionResult()

        assertEquals(WorkoutMainSideEffect.NavigateToRecoveredRecording, viewModel.sideEffect.first())
        assertEquals(WorkoutStatus.ACTIVITY_PAUSED, tracking.state.value.record?.status)
        assertNull(viewModel.uiState.value.recovery)
    }

    /**
     * GIVEN: 복구 모달이 떠 있고 위치 권한이 없을 때
     * WHEN: 이어서 기록을 눌러 시스템 팝업에서 거부하면
     * THEN: 위치 권한 모달을 띄우고, 닫으면 복구 모달로 돌아간다. 기록은 그대로 둔다
     */
    @Test
    fun `이어서 기록에서 위치 권한을 거부하면 안내 뒤 복구 모달로 돌아간다`() = runTest {
        permissions.isLocationRequested = true
        val tracking = FakeWorkoutTrackingRepository(savedRecord = STOPPED_RECORD)
        val viewModel = viewModel(tracking, FakeWorkoutRecordRepository(STOPPED_RECORD))
        viewModel.onLocationPermissionChecked(LocationPermission.DENIED)

        viewModel.onRecoveryContinueClick()
        assertEquals(WorkoutMainSideEffect.RequestLocationPermission, viewModel.sideEffect.first())
        viewModel.onLocationPermissionResult(LocationPermission.DENIED)

        assertTrue(viewModel.uiState.value.isLocationPermissionDialogVisible)
        assertFalse(viewModel.uiState.value.isRecoveryDialogVisible)

        viewModel.onLocationPermissionDialogDismiss()

        assertTrue(viewModel.uiState.value.isRecoveryDialogVisible)
        assertNull(tracking.state.value.record)
    }

    /**
     * GIVEN: 복구 모달이 떠 있을 때
     * WHEN: 종료 후 저장을 누르면
     * THEN: 마지막 기록 시각에 종료하고 경로 전체 캡처를 요청한 뒤, 캡처를 받으면 저장하고 성공 토스트
     */
    @Test
    fun `종료 후 저장은 마지막 기록 시각에 종료하고 캡처해 저장한다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository(
            savedRecord = STOPPED_RECORD,
            savedTrackPoints = listOf(trackPoint(1), trackPoint(2)),
        )
        val viewModel = viewModel(tracking, FakeWorkoutRecordRepository(STOPPED_RECORD))

        viewModel.onRecoverySaveClick()

        assertTrue(viewModel.uiState.value.isRecoverySaving)
        assertNull(viewModel.uiState.value.recovery)
        assertEquals(START.plusSeconds(60), tracking.state.value.record?.endedAt)
        assertEquals(listOf(MapPoint(38.0, 127.0), MapPoint(39.0, 127.0)), viewModel.uiState.value.recoveryCaptureRoute)

        viewModel.onRecoveryMapCaptured(byteArrayOf(1))

        assertEquals(WorkoutMainSideEffect.ShowRecoverySaved, viewModel.sideEffect.first())
        assertFalse(viewModel.uiState.value.isRecoverySaving)
        assertTrue(tracking.isCleared)
    }

    /**
     * GIVEN: 복구 모달이 떠 있을 때
     * WHEN: X를 누르면
     * THEN: 저장, 이어서 기록 없이 기기 기록을 지우고 모달을 닫는다
     */
    @Test
    fun `X를 누르면 기기 기록을 지우고 모달을 닫는다`() = runTest {
        val records = FakeWorkoutRecordRepository(STOPPED_RECORD)
        val viewModel = viewModel(records = records)

        viewModel.onRecoveryDiscardClick()

        assertTrue(records.isCleared)
        assertNull(viewModel.uiState.value.recovery)
        assertNull(withTimeoutOrNull(100) { viewModel.sideEffect.first() })
    }

    /**
     * GIVEN: 종료 후 저장 중일 때
     * WHEN: 지도 캡처가 실패하면
     * THEN: 실패 토스트를 띄우고 기록은 업로드 대기로 남긴다
     */
    @Test
    fun `종료 후 저장에서 캡처가 실패하면 실패 토스트`() = runTest {
        val tracking = FakeWorkoutTrackingRepository(
            savedRecord = STOPPED_RECORD,
            savedTrackPoints = listOf(trackPoint(1), trackPoint(2)),
        )
        val viewModel = viewModel(tracking, FakeWorkoutRecordRepository(STOPPED_RECORD))
        viewModel.onRecoverySaveClick()

        viewModel.onRecoveryMapCaptured(null)

        assertEquals(WorkoutMainSideEffect.ShowRecoverySaveFailed, viewModel.sideEffect.first())
        assertEquals(WorkoutStatus.PENDING_UPLOAD, tracking.state.value.record?.status)
        assertFalse(tracking.isCleared)
    }

    /**
     * GIVEN: 앱이 다시 켜졌고 지도 키를 올려 둔 업로드 대기 기록이 있을 때
     * WHEN: 운동 메인에 들어오면
     * THEN: 다시 보내 저장 토스트를 띄우고 기기 기록을 지운다
     */
    @Test
    fun `업로드 대기 기록이 있으면 다시 보내고 저장 토스트`() = runTest {
        val pending = PENDING_RECORD.copy(mapImageObjectKey = "map-key")
        val tracking = pendingTracking(pending)

        val viewModel = viewModel(tracking, FakeWorkoutRecordRepository(pending))

        assertEquals(WorkoutMainSideEffect.ShowRecoverySaved, viewModel.sideEffect.first())
        assertFalse(viewModel.uiState.value.isCheckingUnfinishedWorkout)
        assertTrue(tracking.isCleared)
    }

    /**
     * GIVEN: 지도 키도 캡처 파일도 없는 업로드 대기 기록일 때
     * WHEN: 운동 메인에 들어오면
     * THEN: 로딩과 함께 경로 전체 캡처를 요청하고, 캡처를 받으면 저장 토스트
     */
    @Test
    fun `캡처가 없는 업로드 대기 기록은 다시 캡처해 보낸다`() = runTest {
        val tracking = pendingTracking(PENDING_RECORD)

        val viewModel = viewModel(tracking, FakeWorkoutRecordRepository(PENDING_RECORD))

        assertTrue(viewModel.uiState.value.isRecoverySaving)
        assertEquals(listOf(MapPoint(38.0, 127.0), MapPoint(39.0, 127.0)), viewModel.uiState.value.recoveryCaptureRoute)

        viewModel.onRecoveryMapCaptured(byteArrayOf(1))

        assertEquals(WorkoutMainSideEffect.ShowRecoverySaved, viewModel.sideEffect.first())
        assertFalse(viewModel.uiState.value.isRecoverySaving)
        assertTrue(tracking.isCleared)
    }

    /**
     * GIVEN: 인터넷이 끊겨 다시 보내기가 실패할 때
     * WHEN: 운동 메인에 들어오면
     * THEN: 안내 없이 기기 기록을 남긴다
     */
    @Test
    fun `다시 보내기가 실패하면 안내 없이 기록을 남긴다`() = runTest {
        val pending = PENDING_RECORD.copy(mapImageObjectKey = "map-key")
        val tracking = pendingTracking(pending)

        val viewModel = viewModel(
            tracking = tracking,
            records = FakeWorkoutRecordRepository(pending),
            remote = FakeWorkoutRemoteRepository(completeError = IOException()),
        )

        assertNull(withTimeoutOrNull(100) { viewModel.sideEffect.first() })
        assertFalse(tracking.isCleared)
    }

    /**
     * GIVEN: 위치 권한을 물어본 적 없을 때
     * WHEN: 처음 확인한 권한이 거부면
     * THEN: 시스템 팝업을 요청하고 물어봤다고 남긴다
     */
    @Test
    fun `처음 진입에 권한이 없으면 한 번 요청한다`() = runTest {
        val viewModel = viewModel()

        viewModel.onLocationPermissionChecked(LocationPermission.DENIED)

        assertEquals(WorkoutMainSideEffect.RequestLocationPermission, viewModel.sideEffect.first())
        assertTrue(permissions.isLocationRequested)
    }

    /**
     * GIVEN: 이미 위치 권한을 물어봤을 때
     * WHEN: 권한이 거부면
     * THEN: 다시 요청하지 않는다
     */
    @Test
    fun `이미 물어봤으면 다시 요청하지 않는다`() = runTest {
        permissions.isLocationRequested = true
        val viewModel = viewModel()

        viewModel.onLocationPermissionChecked(LocationPermission.DENIED)

        assertNull(withTimeoutOrNull(100) { viewModel.sideEffect.first() })
    }

    /**
     * GIVEN: 마지막 위치가 있을 때
     * WHEN: 권한 허용을 두 번 확인하면
     * THEN: 마지막 위치에서 내 위치 따라가기를 한 번만 보내고, 주소를 채운다
     */
    @Test
    fun `권한이 있으면 마지막 위치로 옮기고 주소를 보여준다`() = runTest {
        val viewModel = viewModel(location = FakeLocationRepository(flowOf(LOCATION), lastLocation = LOCATION))

        viewModel.onLocationPermissionChecked(LocationPermission.GRANTED)
        viewModel.onLocationPermissionChecked(LocationPermission.GRANTED)

        assertEquals(WorkoutMainSideEffect.FollowMyLocation(from = MapPoint(37.0, 127.0)), viewModel.sideEffect.first())
        assertNull(withTimeoutOrNull(100) { viewModel.sideEffect.first() })
        assertEquals("영등포구 의사당대로 1", viewModel.uiState.value.address)
        assertFalse(permissions.isLocationRequested)
    }

    /**
     * GIVEN: 이미 물어봤고 권한이 없을 때
     * WHEN: 운동 기록을 눌러 허용하면
     * THEN: 시스템 팝업을 요청하고, 허용 뒤 내 위치로 옮기고 이어서 알림 권한을 요청한다
     */
    @Test
    fun `운동 기록에서 위치 권한을 허용하면 이어서 알림 권한을 요청한다`() = runTest {
        val viewModel = deniedViewModel()

        viewModel.onRecordClick()
        assertEquals(WorkoutMainSideEffect.RequestLocationPermission, viewModel.sideEffect.first())
        viewModel.onLocationPermissionResult(LocationPermission.GRANTED)

        assertEquals(
            listOf(
                WorkoutMainSideEffect.FollowMyLocation(from = null),
                WorkoutMainSideEffect.RequestNotificationPermission,
            ),
            viewModel.sideEffect.take(2).toList(),
        )
    }

    /**
     * GIVEN: 이미 물어봤고 권한이 없을 때
     * WHEN: 운동 기록이나 내 위치를 눌러 거부하면
     * THEN: 안내 모달을 띄운다
     */
    @Test
    fun `탭해서 요청한 권한을 거부하면 안내 모달을 띄운다`() = runTest {
        val viewModel = deniedViewModel()

        viewModel.onMyLocationClick()
        viewModel.onLocationPermissionResult(LocationPermission.DENIED)

        assertTrue(viewModel.uiState.value.isLocationPermissionDialogVisible)
    }

    /**
     * GIVEN: 처음 진입이라 시스템 팝업을 요청했을 때
     * WHEN: 거부하면
     * THEN: 안내 모달은 띄우지 않는다
     */
    @Test
    fun `첫 진입 요청을 거부하면 안내 모달을 띄우지 않는다`() = runTest {
        val viewModel = viewModel()
        viewModel.onLocationPermissionChecked(LocationPermission.DENIED)
        viewModel.sideEffect.first()

        viewModel.onLocationPermissionResult(LocationPermission.DENIED)

        assertFalse(viewModel.uiState.value.isLocationPermissionDialogVisible)
    }

    /**
     * GIVEN: 처음 진입이라 시스템 팝업을 요청했을 때
     * WHEN: 대략적인 위치만 허용하면
     * THEN: 정확한 위치 안내 모달을 바로 띄운다
     */
    @Test
    fun `첫 진입 요청에서 대략적인 위치만 허용하면 안내 모달을 띄운다`() = runTest {
        val viewModel = viewModel()
        viewModel.onLocationPermissionChecked(LocationPermission.DENIED)
        viewModel.sideEffect.first()

        viewModel.onLocationPermissionResult(LocationPermission.APPROXIMATE)

        assertTrue(viewModel.uiState.value.isLocationPermissionDialogVisible)
    }

    /**
     * GIVEN: 위치 권한이 있을 때
     * WHEN: 운동 기록을 누르면
     * THEN: 알림 권한을 요청하고, 결과가 오면 (거부 포함) 안내 없이 기록 화면으로 보낸다
     */
    @Test
    fun `운동 기록을 누르면 알림 권한 다음에 기록 화면으로 간다`() = runTest {
        val viewModel = grantedViewModel()

        viewModel.onRecordClick()
        assertEquals(WorkoutMainSideEffect.RequestNotificationPermission, viewModel.sideEffect.first())
        viewModel.onNotificationPermissionResult()

        assertEquals(WorkoutMainSideEffect.NavigateToRecording, viewModel.sideEffect.first())
        assertFalse(viewModel.uiState.value.isLocationPermissionDialogVisible)
    }

    /**
     * GIVEN: 위치 업데이트 요청이 실패할 때
     * WHEN: 권한 허용을 확인해 주소를 구독하면
     * THEN: 앱이 멈추지 않고 주소는 비어 있다
     */
    @Test
    fun `위치 수신이 실패해도 주소 구독이 앱을 멈추지 않는다`() = runTest {
        val viewModel = viewModel(location = FakeLocationRepository(flow { throw IOException() }))

        viewModel.onLocationPermissionChecked(LocationPermission.GRANTED)

        assertNull(viewModel.uiState.value.address)
    }

    private suspend fun grantedViewModel(
        tracking: FakeWorkoutTrackingRepository = FakeWorkoutTrackingRepository(),
        records: FakeWorkoutRecordRepository = FakeWorkoutRecordRepository(),
    ): WorkoutMainViewModel {
        permissions.isLocationRequested = true
        return viewModel(tracking, records).apply {
            onLocationPermissionChecked(LocationPermission.GRANTED)
            sideEffect.first()
        }
    }

    private fun deniedViewModel(): WorkoutMainViewModel {
        permissions.isLocationRequested = true
        return viewModel().apply { onLocationPermissionChecked(LocationPermission.DENIED) }
    }

    private fun viewModel(
        tracking: FakeWorkoutTrackingRepository = FakeWorkoutTrackingRepository(),
        records: FakeWorkoutRecordRepository = FakeWorkoutRecordRepository(),
        location: FakeLocationRepository = FakeLocationRepository(),
        remote: FakeWorkoutRemoteRepository = FakeWorkoutRemoteRepository(),
    ) = WorkoutMainViewModel(
        savedStateHandle = SavedStateHandle(),
        trackingRepository = tracking,
        locationRepository = location,
        permissionRepository = permissions,
        observeCurrentAddress = ObserveCurrentAddressUseCase(location, FakeReverseGeocodingRepository()),
        getUnfinishedWorkout = GetUnfinishedWorkoutUseCase(tracking, records, clock),
        saveFinishedWorkout = SaveFinishedWorkoutUseCase(
            trackingRepository = tracking,
            recordRepository = records,
            mapImageRepository = FakeMapImageRepository(),
            remoteRepository = remote,
            photoRepository = FakeWorkoutPhotoRepository(),
        ),
        discardWorkout = DiscardWorkoutUseCase(tracking, records, FakeWorkoutPhotoRepository()),
    )

    private fun pendingTracking(record: WorkoutRecord) = FakeWorkoutTrackingRepository(
        savedRecord = record,
        savedTrackPoints = listOf(trackPoint(1), trackPoint(2)),
    )

    private companion object {
        // 1분 기록하고 앱이 꺼진 기록
        val STOPPED_RECORD = record().copy(lastRecordedAt = START.plusSeconds(60))

        // 1분 기록하고 종료했지만 서버 저장을 확인하지 못한 기록
        val PENDING_RECORD = STOPPED_RECORD.finished(START.plusSeconds(60))
        val LOCATION = DeviceLocation(
            latitude = 37.0,
            longitude = 127.0,
            altitude = null,
            altitudeAccuracyMeters = null,
            accuracyMeters = 5f,
            time = START,
        )
    }
}
