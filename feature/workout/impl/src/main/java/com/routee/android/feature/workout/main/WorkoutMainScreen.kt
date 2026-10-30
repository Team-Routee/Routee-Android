package com.routee.android.feature.workout.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeCtaPrimaryButton
import com.routee.android.core.designsystem.component.dialog.RouteeDraftDialog
import com.routee.android.core.designsystem.component.dialog.RouteeTwoButtonDialog
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.routeeMessageAnchor
import com.routee.android.core.designsystem.component.routeeNavBarPadding
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.map.RouteeMapState
import com.routee.android.core.map.RouteeNaverMap
import com.routee.android.core.map.mapLogoAbove
import com.routee.android.core.map.rememberRouteeMapState
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.core.ui.format.formatDistanceKm
import com.routee.android.core.ui.format.formatDurationHourMinuteShort
import com.routee.android.core.ui.loading.RouteeLoadingOverlay
import com.routee.android.feature.workout.R
import com.routee.android.feature.workout.component.main.LocationChip
import com.routee.android.feature.workout.component.main.MyLocationButton
import com.routee.android.feature.workout.component.main.WorkoutTopGradient
import com.routee.android.feature.workout.main.model.LocationPermission
import com.routee.android.feature.workout.main.model.WorkoutMainSideEffect
import com.routee.android.feature.workout.main.model.WorkoutMainUiState
import com.routee.android.feature.workout.main.model.WorkoutRecoveryUiModel
import com.routee.android.feature.workout.model.MY_LOCATION_ZOOM
import com.routee.android.feature.workout.model.captureRouteJpeg
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
internal fun WorkoutMainRoute(
    onNavigateToRecording: (isRecovering: Boolean) -> Unit,
    viewModel: WorkoutMainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapState = rememberRouteeMapState()
    val messageState = LocalRouteeMessageState.current
    val recoveryFailedText = stringResource(R.string.workout_recording_restore_error_toast)
    val recoverySavedText = stringResource(R.string.workout_main_recovery_saved_toast)
    val recoverySaveFailedText = stringResource(R.string.workout_main_recovery_save_failed_toast)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.onLocationPermissionResult(context.locationPermission())
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        viewModel.onNotificationPermissionResult()
    }

    viewModel.sideEffect.CollectSideEffect { sideEffect ->
        when (sideEffect) {
            WorkoutMainSideEffect.NavigateToRecording -> onNavigateToRecording(false)

            WorkoutMainSideEffect.NavigateToRecoveredRecording -> onNavigateToRecording(true)

            WorkoutMainSideEffect.ShowRecoveryFailed -> messageState.showToast(text = recoveryFailedText)

            WorkoutMainSideEffect.ShowRecoverySaved -> messageState.showToast(text = recoverySavedText)

            WorkoutMainSideEffect.ShowRecoverySaveFailed -> messageState.showToast(text = recoverySaveFailedText)

            WorkoutMainSideEffect.RequestLocationPermission -> locationPermissionLauncher.launch(LOCATION_PERMISSIONS)

            WorkoutMainSideEffect.RequestNotificationPermission -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !context.isGranted(Manifest.permission.POST_NOTIFICATIONS)
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.onNotificationPermissionResult()
                }
            }

            is WorkoutMainSideEffect.FollowMyLocation -> scope.launch {
                sideEffect.from?.let { mapState.moveTo(it, zoom = MY_LOCATION_ZOOM) }
                mapState.followMyLocation()
            }
        }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onLocationPermissionChecked(context.locationPermission())
        viewModel.onScreenResumed()
        onPauseOrDispose { viewModel.onScreenPaused() }
    }

    uiState.recoveryCaptureRoute?.let { route ->
        LaunchedEffect(route) { viewModel.onRecoveryMapCaptured(mapState.captureRouteJpeg(route)) }
    }

    // TODO P1 기획: 피그마는 거부 직후 안내, 지금은 운동 기록, 내 위치 탭 때 시스템 팝업을 한 번 더 띄우고(흐름 B) 거부면 안내 (제안)
    val isLocationGranted = uiState.locationPermission == LocationPermission.GRANTED
    WorkoutMainScreen(
        uiState = uiState,
        mapState = mapState,
        // 종료 후 저장 캡처에는 내 위치를 넣지 않음
        myLocation = if (isLocationGranted && !uiState.isRecoverySaving) viewModel.myLocation else null,
        onMyLocationClick = viewModel::onMyLocationClick,
        onRecordClick = viewModel::onRecordClick,
    )

    if (uiState.isLoadingVisible) RouteeLoadingOverlay()

    uiState.recovery?.takeIf { uiState.isRecoveryDialogVisible }?.let { recovery ->
        WorkoutRecoveryDialog(
            recovery = recovery,
            onSaveClick = viewModel::onRecoverySaveClick,
            onContinueClick = viewModel::onRecoveryContinueClick,
            onDiscardClick = viewModel::onRecoveryDiscardClick,
        )
    }

    if (uiState.isLocationPermissionDialogVisible) {
        val isApproximate = uiState.locationPermission == LocationPermission.APPROXIMATE
        // TODO P1 기획: 임시 디자인 (설정으로 이동 버튼이 빨간 글자)
        // TODO P1 기획: 대략적인 위치 안내 문구
        RouteeTwoButtonDialog(
            title = stringResource(
                if (isApproximate) {
                    R.string.workout_main_precise_location_dialog_title
                } else {
                    R.string.workout_main_location_permission_dialog_title
                },
            ),
            description = stringResource(
                if (isApproximate) {
                    R.string.workout_main_precise_location_dialog_description
                } else {
                    R.string.workout_main_location_permission_dialog_description
                },
            ),
            leftButtonText = stringResource(R.string.workout_main_location_permission_dialog_cancel),
            rightButtonText = stringResource(R.string.workout_main_location_permission_dialog_settings),
            onRightButtonClick = {
                viewModel.onLocationPermissionDialogDismiss()
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    ),
                )
            },
            onDismissRequest = viewModel::onLocationPermissionDialogDismiss,
        )
    }
}

@Composable
private fun WorkoutRecoveryDialog(
    recovery: WorkoutRecoveryUiModel,
    onSaveClick: () -> Unit,
    onContinueClick: () -> Unit,
    onDiscardClick: () -> Unit,
) {
    val recordedUntil = remember(recovery.lastRecordedAt) {
        RecoveryTimeFormatter.format(recovery.lastRecordedAt.atZone(ZoneId.systemDefault()))
    }
    RouteeDraftDialog(
        title = stringResource(R.string.workout_main_recovery_title),
        description = stringResource(R.string.workout_main_recovery_recorded_until, recordedUntil),
        metrics = listOf(
            stringResource(R.string.workout_pause_metric_distance_value, formatDistanceKm(recovery.distanceMeters)),
            formatDurationHourMinuteShort(recovery.durationSec),
        ),
        secondaryButtonText = stringResource(R.string.workout_main_recovery_save),
        primaryButtonText = stringResource(R.string.workout_main_recovery_continue),
        onSecondaryButtonClick = onSaveClick,
        onPrimaryButtonClick = onContinueClick,
        onCloseClick = onDiscardClick,
    )
}

// 예: 03/03 오전 3:33
private val RecoveryTimeFormatter = DateTimeFormatter.ofPattern("MM/dd a h:mm", Locale.KOREAN)

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

private fun Context.locationPermission(): LocationPermission = when {
    isGranted(Manifest.permission.ACCESS_FINE_LOCATION) -> LocationPermission.GRANTED
    isGranted(Manifest.permission.ACCESS_COARSE_LOCATION) -> LocationPermission.APPROXIMATE
    else -> LocationPermission.DENIED
}

private fun Context.isGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

@Composable
private fun WorkoutMainScreen(
    uiState: WorkoutMainUiState,
    mapState: RouteeMapState,
    myLocation: Flow<Location>?,
    onMyLocationClick: () -> Unit,
    onRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RouteeNaverMap(
        modifier = modifier.fillMaxSize(),
        state = mapState,
        myLocation = myLocation,
    ) {
        WorkoutTopGradient(modifier = Modifier.align(Alignment.TopCenter))
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = RouteeTheme.spacing.Spacing16),
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                imageVector = ImageVector.vectorResource(DesignSystemR.drawable.routee_logo_mint_md),
                contentDescription = null,
                modifier = Modifier.padding(vertical = RouteeTheme.spacing.Spacing14),
            )
            LocationChip(
                text = if (uiState.locationPermission == LocationPermission.GRANTED && uiState.address != null) {
                    uiState.address
                } else {
                    stringResource(R.string.workout_main_location_permission_required)
                },
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = RouteeTheme.spacing.Spacing16,
                    end = RouteeTheme.spacing.Spacing16,
                    bottom = routeeNavBarPadding(RouteeTheme.spacing.Spacing16),
                ),
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12),
            horizontalAlignment = Alignment.End,
        ) {
            MyLocationButton(onClick = onMyLocationClick)
            RouteeCtaPrimaryButton(
                text = stringResource(R.string.workout_main_record),
                onClick = onRecordClick,
                enabled = !uiState.isCheckingUnfinishedWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .routeeMessageAnchor()
                    .mapLogoAbove(spacing = RouteeTheme.spacing.Spacing12),
            )
        }
    }
}

@PreviewDeviceSizes
@Composable
private fun WorkoutMainScreenPreview() {
    RouteePreviewTheme {
        WorkoutMainScreen(
            uiState = WorkoutMainUiState(address = "영등포구 의사당대로 1", isCheckingUnfinishedWorkout = false),
            mapState = rememberRouteeMapState(),
            myLocation = null,
            onMyLocationClick = {},
            onRecordClick = {},
        )
    }
}
