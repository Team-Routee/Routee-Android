package com.routee.android.feature.recordedit.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.R

/**
 * 기록 편집 색상 팔레트 (디자인시스템 recap-color-palette)
 *
 * 통계 텍스트와 스티커에 함께 적용되며, 최초 기본값은 [DEFAULT]
 */
internal enum class RecapColor(@StringRes val nameRes: Int) {
    ORANGE(R.string.record_edit_editor_color_orange_a11y),
    LIME(R.string.record_edit_editor_color_lime_a11y),
    GREEN(R.string.record_edit_editor_color_green_a11y),
    MINT(R.string.record_edit_editor_color_mint_a11y),
    PURPLE(R.string.record_edit_editor_color_purple_a11y),
    PINK(R.string.record_edit_editor_color_pink_a11y),
    WHITE(R.string.record_edit_editor_color_white_a11y),
    NAVY(R.string.record_edit_editor_color_navy_a11y),
    ;

    companion object {
        val DEFAULT = MINT
    }
}

internal val RecapColor.color: Color
    @Composable
    @ReadOnlyComposable
    get() = with(RouteeTheme.colors) {
        when (this@color) {
            RecapColor.ORANGE -> recapOrange
            RecapColor.LIME -> recapLime
            RecapColor.GREEN -> recapGreen
            RecapColor.MINT -> recapMint
            RecapColor.PURPLE -> recapPurple
            RecapColor.PINK -> recapPink
            RecapColor.WHITE -> recapWhite
            RecapColor.NAVY -> recapNavy
        }
    }
