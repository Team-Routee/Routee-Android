package com.routee.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.routee.android.R
import com.routee.android.core.data.appnotice.model.AppNotice
import com.routee.android.core.designsystem.component.dialog.RouteeOneButtonDialog
import com.routee.android.core.designsystem.component.dialog.RouteeTwoButtonDialog
import com.routee.android.core.designsystem.theme.RouteeTheme

// TODO P1 기획: 업데이트, 점검 모달 문구와 점검 중 앱 사용 여부 확인

/**
 * 업데이트, 점검 안내 다이얼로그. 바깥 영역 탭은 무시
 *
 * @param notice            띄울 안내. 제목, 설명이 null 이면 앱 기본 문구
 * @param onUpdateClick     [업데이트] 탭 시 호출
 * @param onDismissRequest  권장 업데이트 [나중에], 뒤로가기 시 호출
 * @param onExitRequest     점검 [확인], 점검과 강제 업데이트에서 뒤로가기 시 호출
 */
@Composable
internal fun AppNoticeDialog(
    notice: AppNotice,
    onUpdateClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onExitRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (notice) {
        is AppNotice.Maintenance -> RouteeOneButtonDialog(
            title = notice.title ?: stringResource(R.string.app_notice_maintenance_title),
            buttonText = stringResource(R.string.app_notice_confirm),
            onButtonClick = onExitRequest,
            onDismissRequest = onExitRequest,
            modifier = modifier,
            description = notice.message ?: stringResource(R.string.app_notice_maintenance_description),
            dismissOnDimClick = false,
        )

        is AppNotice.ForceUpdate -> RouteeOneButtonDialog(
            title = notice.title ?: stringResource(R.string.app_notice_force_update_title),
            buttonText = stringResource(R.string.app_notice_update),
            onButtonClick = onUpdateClick,
            onDismissRequest = onExitRequest,
            modifier = modifier,
            description = notice.message ?: stringResource(R.string.app_notice_force_update_description),
            dismissOnDimClick = false,
        )

        is AppNotice.OptionalUpdate -> RouteeTwoButtonDialog(
            title = notice.title ?: stringResource(R.string.app_notice_optional_update_title),
            leftButtonText = stringResource(R.string.app_notice_later),
            rightButtonText = stringResource(R.string.app_notice_update),
            onRightButtonClick = {
                onUpdateClick()
                onDismissRequest()
            },
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            description = notice.message ?: stringResource(R.string.app_notice_optional_update_description),
            leftButtonContentColor = RouteeTheme.colors.white60,
            rightButtonContentColor = RouteeTheme.colors.white,
            dismissOnDimClick = false,
        )
    }
}
