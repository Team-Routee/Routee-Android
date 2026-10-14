package com.routee.android.core.designsystem.preview

import androidx.compose.ui.tooling.preview.Preview

/**
 * 기본 글자 크기와 200%를 함께 보여주는 Preview 묶음
 *
 * 컴포넌트 Preview에 `@Preview` 대신 붙인다
 */
@Preview(widthDp = 360)
@Preview(name = "글자 200%", widthDp = 360, fontScale = 2f)
annotation class PreviewFontSizes
