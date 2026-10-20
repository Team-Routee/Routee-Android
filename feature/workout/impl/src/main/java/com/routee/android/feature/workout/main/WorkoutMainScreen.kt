package com.routee.android.feature.workout.main

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeCtaPrimaryButton
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
import com.routee.android.feature.workout.main.model.WorkoutMainUiState

@Composable
internal fun WorkoutMainRoute(
    onRecordClick: () -> Unit,
    viewModel: WorkoutMainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutMainScreen(
        uiState = uiState,
        // TODO 지도를 내 위치로 이동
        onMyLocationClick = {},
        // TODO 위치 권한 없으면 안내 다이얼로그
        onRecordClick = onRecordClick,
    )
}

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
                text = if (uiState.isLocationPermissionGranted) {
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
