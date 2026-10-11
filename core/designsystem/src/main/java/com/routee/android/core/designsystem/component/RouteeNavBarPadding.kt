package com.routee.android.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 하단 탭 바가 차지하는 높이. 탭 바가 없으면 0
 *
 * 화면에서는 [routeeNavBarPadding] 사용
 */
val LocalRouteeNavBarPadding = compositionLocalOf { 0.dp }

/**
 * 탭 바에 가려지지 않는 하단 여백
 *
 * ```kotlin
 * // 스크롤 내용: 끝에만 여백
 * LazyColumn(contentPadding = PaddingValues(bottom = routeeNavBarPadding(20.dp))) { ... }
 * // 고정 요소: 탭 바 위로
 * Button(modifier = Modifier.padding(bottom = routeeNavBarPadding(16.dp))) { ... }
 * ```
 *
 * @param extra 탭 바 위 간격
 */
@Composable
@ReadOnlyComposable
fun routeeNavBarPadding(extra: Dp = 0.dp): Dp = LocalRouteeNavBarPadding.current + extra
