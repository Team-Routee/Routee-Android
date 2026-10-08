package com.routee.android.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.routee.android.core.designsystem.R

internal val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
)

/* TODO Coolvetica 임시 폰트 라이선스 확인 후 변경 필요 */
internal val DisplayFontFamily: FontFamily = FontFamily.Default

private fun pretendard(weight: FontWeight, size: Int) = TextStyle(
    fontFamily = Pretendard,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = 1.4.em, // 140%
    letterSpacing = (-0.01).em, // -1.0%
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

private fun display(size: Int) = TextStyle(
    fontFamily = DisplayFontFamily,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = 1.2.em, // 120%
    letterSpacing = 0.04.em, // 4%
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

@Immutable
data class RouteeTypography(
    val display52: TextStyle,
    val display40: TextStyle,
    val display30: TextStyle,
    val display26: TextStyle,
    val titleSb24: TextStyle,
    val titleSb20: TextStyle,
    val titleSb18: TextStyle,
    val bodyR16: TextStyle,
    val bodySb14: TextStyle,
    val labelSb18: TextStyle,
    val labelSb16: TextStyle,
    val labelM16: TextStyle,
    val labelSb14: TextStyle,
    val labelM14: TextStyle,
    val labelR14: TextStyle,
    val labelSb12: TextStyle,
    val labelM12: TextStyle,
    val labelR12: TextStyle,
)

val DefaultRouteeTypography = RouteeTypography(
    // Display — Italic, 120%, 4%
    display52 = display(52),
    display40 = display(40),
    display30 = display(30),
    display26 = display(26),

    // Title - Pretendard, 140%, -1%
    titleSb24 = pretendard(FontWeight.SemiBold, 24),
    titleSb20 = pretendard(FontWeight.SemiBold, 20),
    titleSb18 = pretendard(FontWeight.SemiBold, 18),

    // Body
    bodyR16 = pretendard(FontWeight.Normal, 16),
    bodySb14 = pretendard(FontWeight.SemiBold, 14),

    // Label
    labelSb18 = pretendard(FontWeight.SemiBold, 18),
    labelSb16 = pretendard(FontWeight.SemiBold, 16),
    labelM16 = pretendard(FontWeight.Medium, 16),
    labelSb14 = pretendard(FontWeight.SemiBold, 14),
    labelM14 = pretendard(FontWeight.Medium, 14),
    labelR14 = pretendard(FontWeight.Normal, 14),
    labelSb12 = pretendard(FontWeight.SemiBold, 12),
    labelM12 = pretendard(FontWeight.Medium, 12),
    labelR12 = pretendard(FontWeight.Normal, 12),
)

internal val LocalRouteeTypography = staticCompositionLocalOf { DefaultRouteeTypography }
