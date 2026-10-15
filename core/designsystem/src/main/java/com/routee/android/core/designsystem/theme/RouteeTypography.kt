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

internal val Archivo = FontFamily(
    Font(R.font.archivo_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.archivo_bold_italic, FontWeight.Bold, FontStyle.Italic),
)

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

private fun display(weight: FontWeight, size: Int, lineHeight: Double) = TextStyle(
    fontFamily = Archivo,
    fontStyle = FontStyle.Italic,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.em,
    letterSpacing = (-0.04).em, // -4%
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

@Immutable
data class RouteeTypography(
    val displayB42: TextStyle,
    val displaySb28: TextStyle,
    val displaySb22: TextStyle,
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
    // Display - Archivo Italic, -4%
    displayB42 = display(FontWeight.Bold, 42, lineHeight = 1.3), // 130%
    displaySb28 = display(FontWeight.SemiBold, 28, lineHeight = 1.3), // 130%
    displaySb22 = display(FontWeight.SemiBold, 22, lineHeight = 1.2), // 120%

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
