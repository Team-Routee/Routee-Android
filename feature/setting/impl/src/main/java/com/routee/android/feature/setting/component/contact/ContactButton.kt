package com.routee.android.feature.setting.component.contact

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.setting.R

private val ContactIconSize = 24.dp
private val KakaoYellow = Color(0xFFFEE500)

/**
 * 문의 방법 버튼 종류
 */
internal enum class ContactButtonType(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    KAKAO_TALK(
        labelRes = R.string.setting_contact_button_kakao,
        iconRes = DesignSystemR.drawable.ic_kakaotalk,
    ),
    EMAIL(
        labelRes = R.string.setting_contact_button_email,
        iconRes = DesignSystemR.drawable.ic_email,
    ),
}

/**
 * 문의하기 버튼 (btn_contact_android)
 *
 * [type]에 맞는 아이콘, 문구, 색을 보여준다. 폭은 [modifier]로 정한다
 */
@Composable
internal fun ContactButton(
    type: ContactButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RouteeTheme.colors
    val shape = RoundedCornerShape(RouteeTheme.radius.Radius12)
    val typeModifier = when (type) {
        ContactButtonType.KAKAO_TALK -> Modifier.background(KakaoYellow)

        ContactButtonType.EMAIL ->
            Modifier
                .background(colors.bgPrimary)
                .border(RouteeTheme.border.Thickness, colors.white30, shape)
    }

    Row(
        modifier = modifier
            .clip(shape)
            .then(typeModifier)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = RouteeTheme.spacing.Spacing16, vertical = RouteeTheme.spacing.Spacing12),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(type.iconRes),
            contentDescription = null,
            modifier = Modifier.size(ContactIconSize),
        )
        Text(
            text = stringResource(type.labelRes),
            style = RouteeTheme.typography.labelSb14,
            color = if (type == ContactButtonType.KAKAO_TALK) colors.black else colors.white,
        )
    }
}

@PreviewFontSizes
@Composable
private fun ContactButtonPreview() {
    RouteePreviewTheme(modifier = Modifier.padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ContactButton(type = ContactButtonType.KAKAO_TALK, onClick = {}, modifier = Modifier.fillMaxWidth())
            ContactButton(type = ContactButtonType.EMAIL, onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
