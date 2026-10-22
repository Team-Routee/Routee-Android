package com.routee.android.feature.workout.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.runtime.getValue
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
import com.routee.android.core.designsystem.component.dialog.RouteeTwoButtonDialog
import com.routee.android.core.designsystem.component.message.routeeMessageAnchor
import com.routee.android.core.designsystem.component.routeeNavBarPadding
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.map.RouteeNaverMap
import com.routee.android.core.map.mapLogoAbove
import com.routee.android.feature.workout.R
import com.routee.android.feature.workout.component.main.LocationChip
import com.routee.android.feature.workout.component.main.MyLocationButton
import com.routee.android.feature.workout.component.main.WorkoutTopGradient
import com.routee.android.feature.workout.main.model.LocationPermission
import com.routee.android.feature.workout.main.model.WorkoutMainUiState

@Composable
internal fun WorkoutMainRoute(
    onRecordClick: () -> Unit,
    viewModel: WorkoutMainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        val permission = context.locationPermission()
        viewModel.onLocationPermissionChecked(permission)
        if (permission != LocationPermission.GRANTED) viewModel.showLocationPermissionDialog()
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onLocationPermissionChecked(context.locationPermission())
        onPauseOrDispose {}
    }

    // TODO P1 기획: 피그마는 홈 진입 시 요청하고 거부 직후 안내, 지금은 운동 기록, 내 위치 탭 시 요청하고 거부면 안내 (제안)
    val isLocationGranted = uiState.locationPermission == LocationPermission.GRANTED
    WorkoutMainScreen(
        uiState = uiState,
        onMyLocationClick = {
            // TODO 지도를 내 위치로 이동
            if (!isLocationGranted) locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
        },
        onRecordClick = {
            if (isLocationGranted) onRecordClick() else locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
        },
    )

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
    onMyLocationClick: () -> Unit,
    onRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RouteeNaverMap(modifier = modifier.fillMaxSize()) {
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
                text = if (uiState.locationPermission == LocationPermission.GRANTED) {
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
            uiState = WorkoutMainUiState(address = "영등포구 의사당대로 1"),
            onMyLocationClick = {},
            onRecordClick = {},
        )
    }
}
