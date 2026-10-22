package com.routee.android.feature.recordedit.complete

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeAsyncImage
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.routeeMessageAnchor
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.feature.recordedit.R
import com.routee.android.feature.recordedit.complete.model.RecapCompleteSideEffect
import com.routee.android.feature.recordedit.component.complete.RecapActionButton
import java.io.File

// recap_image
private const val RECAP_ASPECT_RATIO = 9f / 16f
private val RecapImageHorizontalPadding = 30.dp
private val ActionButtonTopSpacing = 35.dp

@Composable
internal fun RecapCompleteRoute(
    viewModel: RecapCompleteViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val context = LocalContext.current
    val messageState = LocalRouteeMessageState.current
    val saveSuccessMessage = stringResource(R.string.record_edit_complete_save_success_toast)
    val saveFailureMessage = stringResource(R.string.record_edit_complete_save_failure_toast)
    val noPermissionMessage = stringResource(R.string.record_edit_complete_no_permission_toast)
    val imageUri = remember(viewModel.imagePath) { File(viewModel.imagePath).toUri() }
    val storagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) viewModel.onDownloadClick() else messageState.showToast(text = noPermissionMessage)
    }

    viewModel.sideEffect.CollectSideEffect { sideEffect ->
        when (sideEffect) {
            RecapCompleteSideEffect.NavigateBack -> onNavigateBack()
            RecapCompleteSideEffect.NavigateToHome -> onNavigateToHome()
            is RecapCompleteSideEffect.ShareImage -> context.startActivity(sideEffect.imageUri.toShareIntent())
            RecapCompleteSideEffect.ShowSaveSuccessToast -> messageState.showToast(text = saveSuccessMessage)
            RecapCompleteSideEffect.ShowSaveFailureToast -> messageState.showToast(text = saveFailureMessage)
        }
    }

    BackHandler(onBack = viewModel::onBackClick)

    RecapCompleteScreen(
        imageUrl = imageUri.toString(),
        onBackClick = viewModel::onBackClick,
        onCompleteClick = viewModel::onCompleteClick,
        onDownloadClick = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                viewModel.onDownloadClick()
            }
        },
        onShareClick = viewModel::onShareClick,
    )
}

@Composable
private fun RecapCompleteScreen(
    imageUrl: String?,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RouteeBgEllipse(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RouteePageNavBar(
                onBackClick = onBackClick,
                actions = {
                    Text(
                        text = stringResource(R.string.record_edit_editor_complete),
                        style = RouteeTheme.typography.labelSb18,
                        color = RouteeTheme.colors.white,
                        modifier = Modifier
                            .noRippleClickable(role = Role.Button, onClick = onCompleteClick)
                            .padding(
                                horizontal = RouteeTheme.spacing.Spacing16,
                                vertical = RouteeTheme.spacing.Spacing10,
                            ),
                    )
                },
            )
            Spacer(modifier = Modifier.height(RouteeTheme.spacing.Spacing24))
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = RecapImageHorizontalPadding)
                    .aspectRatio(RECAP_ASPECT_RATIO),
            ) {
                RouteeAsyncImage(
                    url = imageUrl,
                    contentDescription = stringResource(R.string.record_edit_complete_image_a11y),
                    modifier = Modifier.fillMaxSize(),
                )
                Spacer(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .routeeMessageAnchor(),
                )
            }
            Spacer(modifier = Modifier.height(ActionButtonTopSpacing))
            Row(horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing20)) {
                RecapActionButton(
                    iconRes = DesignSystemR.drawable.ic_download_recap_lg,
                    contentDescription = stringResource(R.string.record_edit_complete_download_a11y),
                    onClick = onDownloadClick,
                )
                RecapActionButton(
                    iconRes = DesignSystemR.drawable.ic_share_recap_lg,
                    contentDescription = stringResource(R.string.record_edit_complete_share_a11y),
                    onClick = onShareClick,
                )
            }
        }
    }
}

private fun Uri.toShareIntent(): Intent {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, this@toShareIntent)
        clipData = ClipData.newRawUri(null, this@toShareIntent)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(sendIntent, null)
}

@PreviewDeviceSizes
@Composable
private fun RecapCompleteScreenPreview() {
    RouteePreviewTheme {
        RecapCompleteScreen(
            imageUrl = null,
            onBackClick = {},
            onCompleteClick = {},
            onDownloadClick = {},
            onShareClick = {},
        )
    }
}
