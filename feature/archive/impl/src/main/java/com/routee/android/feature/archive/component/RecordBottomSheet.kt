package com.routee.android.feature.archive.component

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeAsyncImage
import com.routee.android.core.designsystem.component.message.RouteeToast
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

private const val MAX_HEIGHT_RECORD_COUNT = 3
private val ContentMaxHeight = 304.dp
private val HandleWidth = 40.dp
private val HandleHeight = 4.dp
private val ThumbnailSize = 42.dp
private val DefaultThumbnailSymbolWidth = 22.dp

/**
 * 기록 목록 바텀시트 항목
 *
 * @param activityId    기록 id
 * @param title         기록 제목
 * @param thumbnailUrl  대표 사진. null 이면 기본 썸네일
 */
@Immutable
internal data class RecordSheetItem(
    val activityId: Long,
    val title: String,
    val thumbnailUrl: String?,
)

/**
 * 기록 목록 바텀시트 (record_bottom_sheet_android)
 *
 * @param date              시트 상단 날짜 (예: "2026.03.12")
 * @param records           그날 기록 목록. 넘치면 시트 안에서 스크롤
 * @param onRecordClick     기록 탭 시 activityId 전달
 * @param onDismissRequest  아래로 끌기, 뒤로가기, 바깥 영역 탭 시 호출
 * @param message           시트 안에 표시할 안내 문구
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordBottomSheet(
    date: String,
    records: List<RecordSheetItem>,
    onRecordClick: (Long) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(
            topStart = RouteeTheme.radius.Radius16,
            topEnd = RouteeTheme.radius.Radius16,
        ),
        containerColor = RouteeTheme.colors.grey900,
        contentColor = RouteeTheme.colors.white,
        scrimColor = RouteeTheme.colors.dimSecondary,
        dragHandle = { SheetHandle() },
    ) {
        DisableNavigationBarContrast()
        Box {
            RecordBottomSheetContent(
                date = date,
                records = records,
                onRecordClick = onRecordClick,
            )
            AnimatedContent(
                targetState = message,
                contentAlignment = Alignment.BottomCenter,
                transitionSpec = { (fadeIn() togetherWith fadeOut()).using(sizeTransform = null) },
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(horizontal = RouteeTheme.spacing.Spacing24, vertical = RouteeTheme.spacing.Spacing16)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                label = "RecordSheetMessage",
            ) { text ->
                if (text != null) RouteeToast(text)
            }
        }
    }
}

// 3버튼 내비게이션의 반투명 대비 배경 끔 (앱 enableEdgeToEdge와 같게)
@Composable
private fun DisableNavigationBarContrast() {
    val window = (LocalView.current.parent as? DialogWindowProvider)?.window
    SideEffect {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window?.isNavigationBarContrastEnforced = false
    }
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = RouteeTheme.spacing.Spacing14)
            .size(width = HandleWidth, height = HandleHeight)
            .background(color = RouteeTheme.colors.grey300, shape = RoundedCornerShape(RouteeTheme.radius.Full)),
    )
}

@Composable
private fun RecordBottomSheetContent(
    date: String,
    records: List<RecordSheetItem>,
    onRecordClick: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .then(if (records.size >= MAX_HEIGHT_RECORD_COUNT) Modifier.height(ContentMaxHeight) else Modifier)
            .padding(horizontal = RouteeTheme.spacing.Spacing24),
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing16),
    ) {
        Text(
            text = date,
            style = RouteeTheme.typography.labelSb14,
            color = RouteeTheme.colors.white,
        )
        // record_list_container
        LazyColumn(
            modifier = Modifier
                .weight(weight = 1f, fill = false)
                .nestedScroll(ConsumeRemainingFling),
            contentPadding = PaddingValues(bottom = RouteeTheme.spacing.Spacing16),
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12),
        ) {
            items(items = records, key = { it.activityId }) { record ->
                RecordListItem(
                    record = record,
                    onClick = { onRecordClick(record.activityId) },
                )
            }
        }
    }
}

// 목록 fling 남은 속도가 시트로 넘어가지 않게
private val ConsumeRemainingFling = object : NestedScrollConnection {
    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
}

// record_list_item
@Composable
private fun RecordListItem(
    record: RecordSheetItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = RouteeTheme.colors.white10, shape = RoundedCornerShape(RouteeTheme.radius.Radius12))
            .noRippleClickable(role = Role.Button, onClick = onClick)
            .padding(RouteeTheme.spacing.Spacing12),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecordThumbnail(url = record.thumbnailUrl)
        Text(
            text = record.title,
            style = RouteeTheme.typography.labelM14,
            color = RouteeTheme.colors.white,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Image(
            imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_chevron_right_sm_grey),
            contentDescription = null,
        )
    }
}

// calendar_day_cell
@Composable
private fun RecordThumbnail(url: String?) {
    val shape = RoundedCornerShape(RouteeTheme.radius.Radius12)
    if (url.isNullOrBlank()) {
        // TODO: 기본 썸네일 에셋 받으면 교체
        Box(
            modifier = Modifier
                .size(ThumbnailSize)
                .background(color = RouteeTheme.colors.grey900, shape = shape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                imageVector = ImageVector.vectorResource(DesignSystemR.drawable.routee_symbol_grey_sm),
                contentDescription = null,
                modifier = Modifier.width(DefaultThumbnailSymbolWidth),
            )
        }
    } else {
        RouteeAsyncImage(
            url = url,
            contentDescription = null,
            modifier = Modifier
                .size(ThumbnailSize)
                .clip(shape),
            errorBackgroundColor = RouteeTheme.colors.grey900,
        )
    }
}

@PreviewFontSizes
@Composable
private fun RecordBottomSheetPreview() {
    RouteePreviewTheme {
        Column(
            modifier = Modifier
                .width(360.dp)
                .background(
                    color = RouteeTheme.colors.grey900,
                    shape = RoundedCornerShape(
                        topStart = RouteeTheme.radius.Radius16,
                        topEnd = RouteeTheme.radius.Radius16,
                    ),
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SheetHandle()
            RecordBottomSheetContent(
                date = "2026.03.12",
                records = listOf(
                    RecordSheetItem(activityId = 1, title = "숭실대 동기모임 북악산", thumbnailUrl = null),
                    RecordSheetItem(
                        activityId = 2,
                        title = "백석동천 저녁 산책",
                        thumbnailUrl = "https://example.com/photo.jpg",
                    ),
                ),
                onRecordClick = {},
            )
        }
    }
}
