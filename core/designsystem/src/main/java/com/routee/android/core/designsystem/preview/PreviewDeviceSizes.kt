package com.routee.android.core.designsystem.preview

import androidx.compose.ui.tooling.preview.Preview

/**
 * 화면 크기별 Preview 묶음 (기본, 큰 글자, 짧은 폰, 폴더블)
 *
 * 화면 Preview에 붙인다. 높이는 시스템 바를 뺀 값
 */
@Preview(name = "1. Phone", device = "spec:width=360dp,height=736dp,dpi=480")
@Preview(name = "2. Phone - Large Font", device = "spec:width=360dp,height=736dp,dpi=480", fontScale = 2f)
@Preview(name = "3. Phone - Short", device = "spec:width=360dp,height=568dp,dpi=480")
@Preview(name = "4. Foldable", device = "spec:width=673dp,height=769dp,dpi=420")
annotation class PreviewDeviceSizes
