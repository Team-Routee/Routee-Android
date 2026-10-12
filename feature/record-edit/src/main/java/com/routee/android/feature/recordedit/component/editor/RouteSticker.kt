package com.routee.android.feature.recordedit.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.model.RecapColor
import com.routee.android.feature.recordedit.model.color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 루트 스티커 (route_sticker)
 *
 * 위치 아이콘 아래로 경유지 이름을 순서대로 나열한다. 아이콘과 글자에 같은 색이 적용된다
 *
 * @param routeNames 루트 순번(sequence) 순서로 정렬된 경유지 이름
 * @param recapColor 색상 팔레트에서 선택된 색
 */
@Composable
internal fun RouteSticker(
    routeNames: ImmutableList<String>,
    recapColor: RecapColor,
    modifier: Modifier = Modifier,
) {
    val contentColor = recapColor.color

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing6),
    ) {
        Icon(
            painter = painterResource(DesignSystemR.drawable.route_leading_graphic),
            contentDescription = null,
            tint = contentColor,
        )
        routeNames.forEach { routeName ->
            RouteStickerItem(
                routeName = routeName,
                color = contentColor,
                modifier = Modifier.padding(start = RouteeTheme.spacing.Spacing4),
            )
        }
    }
}

/**
 * 루트 스티커의 경유지 1개 (route_sticker_item)
 */
@Composable
private fun RouteStickerItem(
    routeName: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = routeName,
        style = RouteeTheme.typography.labelM12,
        color = color,
        maxLines = 1,
        modifier = modifier,
    )
}

private class RouteStickerPreviewParameterProvider : PreviewParameterProvider<RecapColor> {
    override val values = sequenceOf(RecapColor.DEFAULT, RecapColor.ORANGE, RecapColor.WHITE)
}

@Preview
@Composable
private fun RouteStickerPreview(
    @PreviewParameter(RouteStickerPreviewParameterProvider::class) recapColor: RecapColor,
) {
    RouteeTheme {
        RouteSticker(
            routeNames = persistentListOf(
                "창의문",
                "청운대",
                "말바위 전망대에서 한 컷",
                "북악산 내려가는 길",
                "오늘의 등산 완료!",
            ),
            recapColor = recapColor,
            modifier = Modifier.background(RouteeTheme.colors.bgPrimary),
        )
    }
}
