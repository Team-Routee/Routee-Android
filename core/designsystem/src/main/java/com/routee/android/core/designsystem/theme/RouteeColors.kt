package com.routee.android.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
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
    val bgCtaPrimary: Color,
    val bgCtaSecondary: Color,
    val recapOrange: Color,
    val recapLime: Color,
    val recapGreen: Color,
    val recapMint: Color,
    val recapPurple: Color,
    val recapPink: Color,
    val recapWhite: Color,
    val recapNavy: Color,
)

private val SurfaceTinted = Color(0x1A34E5F2)

val DefaultRouteeColors = RouteeColors(
    // Semantic — color/brand
    brandPrimary = RouteePalette.Mint500,
    brandSecondary = RouteePalette.Lime300,

    // Semantic — color/bg
    bgPrimary = RouteePalette.Black,

    // Semantic — color/surface
    surfaceTinted = SurfaceTinted,

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
    bgCtaSecondary = SurfaceTinted,

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
