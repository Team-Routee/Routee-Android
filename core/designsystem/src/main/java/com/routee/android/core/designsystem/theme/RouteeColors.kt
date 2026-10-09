package com.routee.android.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

internal object RouteePalette {
    // color/mint
    val Mint100 = Color(0xFFCEFEFF)
    val Mint200 = Color(0xFFA3FBFE)
    val Mint300 = Color(0xFF63F6FD)
    val Mint400 = Color(0xFF1DE7F3)
    val Mint500 = Color(0xFF01DEEF)
    val Mint600 = Color(0xFF04A1B6)
    val Mint700 = Color(0xFF0B8093)
    val Mint800 = Color(0xFF136677)
    val Mint900 = Color(0xFF145565)

    // color/lime
    val Lime100 = Color(0xFFF4FFC4)
    val Lime200 = Color(0xFFEEFF8F)
    val Lime300 = Color(0xFFE5FF4F)
    val Lime400 = Color(0xFFDBFA1B)

    // color/grayscale
    val Grey50 = Color(0xFFEDEDEE)
    val Grey200 = Color(0xFFAAACB1)
    val Grey300 = Color(0xFF83868E)
    val Grey400 = Color(0xFF6B6E78)
    val Grey500 = Color(0xFF464A56)
    val Grey600 = Color(0xFF40434E)
    val Grey800 = Color(0xFF27292F)
    val Grey900 = Color(0xFF1D1F24)

    // color/static
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF101113)

    // color/alpha/white
    val White60 = Color(0x99FFFFFF)
    val White30 = Color(0x4DFFFFFF)
    val White10 = Color(0x1AFFFFFF)

    // color/alpha/black
    val Black80 = Color(0xCC101113)
    val Black60 = Color(0x99101113)
    val Black50 = Color(0x80101113)
    val Black40 = Color(0x66101113)
    val Black30 = Color(0x4D101113)
}

@Immutable
data class RouteeColors(
    // Primitive
    val mint100: Color,
    val mint200: Color,
    val mint300: Color,
    val mint400: Color,
    val mint500: Color,
    val mint600: Color,
    val mint700: Color,
    val mint800: Color,
    val mint900: Color,
    val lime100: Color,
    val lime200: Color,
    val lime300: Color,
    val lime400: Color,
    val grey50: Color,
    val grey200: Color,
    val grey300: Color,
    val grey400: Color,
    val grey500: Color,
    val grey600: Color,
    val grey800: Color,
    val grey900: Color,
    val white: Color,
    val black: Color,
    val white60: Color,
    val white30: Color,
    val white10: Color,
    val black80: Color,
    val black60: Color,
    val black50: Color,
    val black40: Color,
    val black30: Color,

    // Semantic
    val brandPrimary: Color,
    val brandSecondary: Color,
    val bgPrimary: Color,
    val surfaceTinted: Color,
    val dimPrimary: Color,
    val dimSecondary: Color,
    val statusError: Color,
    val statusWarning: Color,
    val statusSuccess: Color,
    val statusInfo: Color,

    // Component
    val bgCtaPrimary: Color,
    val bgCtaSecondary: Color,
    val bgHighlight: Brush,
    val recapOrange: Color,
    val recapLime: Color,
    val recapGreen: Color,
    val recapMint: Color,
    val recapPurple: Color,
    val recapPink: Color,
    val recapWhite: Color,
    val recapNavy: Color,
)

val DefaultRouteeColors = RouteeColors(
    // Primitive — color/mint
    mint100 = RouteePalette.Mint100,
    mint200 = RouteePalette.Mint200,
    mint300 = RouteePalette.Mint300,
    mint400 = RouteePalette.Mint400,
    mint500 = RouteePalette.Mint500,
    mint600 = RouteePalette.Mint600,
    mint700 = RouteePalette.Mint700,
    mint800 = RouteePalette.Mint800,
    mint900 = RouteePalette.Mint900,

    // Primitive — color/lime
    lime100 = RouteePalette.Lime100,
    lime200 = RouteePalette.Lime200,
    lime300 = RouteePalette.Lime300,
    lime400 = RouteePalette.Lime400,

    // Primitive — color/grayscale
    grey50 = RouteePalette.Grey50,
    grey200 = RouteePalette.Grey200,
    grey300 = RouteePalette.Grey300,
    grey400 = RouteePalette.Grey400,
    grey500 = RouteePalette.Grey500,
    grey600 = RouteePalette.Grey600,
    grey800 = RouteePalette.Grey800,
    grey900 = RouteePalette.Grey900,

    // Primitive — color/static
    white = RouteePalette.White,
    black = RouteePalette.Black,

    // Primitive — color/alpha/white
    white60 = RouteePalette.White60,
    white30 = RouteePalette.White30,
    white10 = RouteePalette.White10,

    // Primitive — color/alpha/black
    black80 = RouteePalette.Black80,
    black60 = RouteePalette.Black60,
    black50 = RouteePalette.Black50,
    black40 = RouteePalette.Black40,
    black30 = RouteePalette.Black30,

    // Semantic — color/brand
    brandPrimary = RouteePalette.Mint500,
    brandSecondary = RouteePalette.Lime300,

    // Semantic — color/bg
    bgPrimary = RouteePalette.Black,

    // Semantic — color/surface
    surfaceTinted = Color(0x1A34E5F2),

    // Semantic — color/dim
    dimPrimary = RouteePalette.Black60,
    dimSecondary = RouteePalette.Black80,

    // Semantic — color/status
    statusError = Color(0xFFDC4949),
    statusWarning = Color(0xFFF59E0B),
    statusSuccess = Color(0xFF22C55E),
    statusInfo = Color(0xFF0A86DF),

    // Component — color/bg (bg-cta)
    bgCtaPrimary = RouteePalette.Mint300,
    bgCtaSecondary = Color(0x1A34E5F2),

    // Component — bg-highlight
    bgHighlight = OvalRadialGradient(listOf(RouteePalette.Lime300, RouteePalette.Mint500)),

    // Component — recap-color-palette
    recapOrange = Color(0xFFF8591F),
    recapLime = RouteePalette.Lime300,
    recapGreen = Color(0xFF73EFAF),
    recapMint = RouteePalette.Mint300,
    recapPurple = Color(0xFFAB9EFF),
    recapPink = Color(0xFFFFA3BC),
    recapWhite = RouteePalette.White,
    recapNavy = Color(0xFF192031),
)

internal val LocalRouteeColors = staticCompositionLocalOf { DefaultRouteeColors }
