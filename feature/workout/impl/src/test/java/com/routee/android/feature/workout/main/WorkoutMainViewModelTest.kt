package com.routee.android.feature.workout.main

import androidx.lifecycle.SavedStateHandle
import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.domain.location.ObserveCurrentAddressUseCase
import com.routee.android.core.map.model.MapPoint
import com.routee.android.feature.workout.FakeLocationRepository
import com.routee.android.feature.workout.FakePermissionRepository
import com.routee.android.feature.workout.FakeReverseGeocodingRepository
import com.routee.android.feature.workout.FakeWorkoutTrackingRepository
import com.routee.android.feature.workout.MainDispatcherRule
import com.routee.android.feature.workout.START
import com.routee.android.feature.workout.main.model.LocationPermission
import com.routee.android.feature.workout.main.model.WorkoutMainSideEffect
import com.routee.android.feature.workout.record
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WorkoutMainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val permissions = FakePermissionRepository()

    /**
     * GIVEN: 기기에 진행 중인 기록이 있을 때
     * WHEN: 운동 메인에 들어오면
     * THEN: 기록을 복원하고 기록 화면으로 보낸다
     */
    @Test
    fun `진행 중인 기록이 있으면 기록 화면으로 보낸다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository(savedRecord = record())

        val viewModel = viewModel(tracking)

        assertTrue(tracking.isRestored)
        assertEquals(WorkoutMainSideEffect.NavigateToRecording, viewModel.sideEffect.first())
    }

    /**
     * GIVEN: 진행 중인 기록이 없을 때
     * WHEN: 운동 메인에 들어오면
     * THEN: 홈에 머문다
     */
    @Test
    fun `진행 중인 기록이 없으면 홈에 머문다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository()

        val viewModel = viewModel(tracking)

        assertTrue(tracking.isRestored)
        assertNull(withTimeoutOrNull(100) { viewModel.sideEffect.first() })
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
     * THEN: 시스템 팝업을 요청하고, 허용 뒤 내 위치로 옮기고 기록 화면으로 보낸다
     */
    @Test
    fun `운동 기록에서 권한을 허용하면 기록 화면으로 간다`() = runTest {
        val viewModel = deniedViewModel()

        viewModel.onRecordClick()
        assertEquals(WorkoutMainSideEffect.RequestLocationPermission, viewModel.sideEffect.first())
        viewModel.onLocationPermissionResult(LocationPermission.GRANTED)

        assertEquals(
            listOf(WorkoutMainSideEffect.FollowMyLocation(from = null), WorkoutMainSideEffect.NavigateToRecording),
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
     * GIVEN: 권한이 있을 때
     * WHEN: 운동 기록을 누르면
     * THEN: 팝업 없이 기록 화면으로 보낸다
     */
    @Test
    fun `권한이 있으면 운동 기록을 누를 때 바로 기록 화면으로 간다`() = runTest {
        permissions.isLocationRequested = true
        val viewModel = viewModel()
        viewModel.onLocationPermissionChecked(LocationPermission.GRANTED)
        viewModel.sideEffect.first()

        viewModel.onRecordClick()

        assertEquals(WorkoutMainSideEffect.NavigateToRecording, viewModel.sideEffect.first())
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

    private fun deniedViewModel(): WorkoutMainViewModel {
        permissions.isLocationRequested = true
        return viewModel().apply { onLocationPermissionChecked(LocationPermission.DENIED) }
    }

    private fun viewModel(
        tracking: FakeWorkoutTrackingRepository = FakeWorkoutTrackingRepository(),
        location: FakeLocationRepository = FakeLocationRepository(),
    ) = WorkoutMainViewModel(
        savedStateHandle = SavedStateHandle(),
        trackingRepository = tracking,
        locationRepository = location,
        permissionRepository = permissions,
        observeCurrentAddress = ObserveCurrentAddressUseCase(location, FakeReverseGeocodingRepository()),
    )

    private companion object {
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
