# Window Insets

| Screen | Top | Bottom | Keyboard |
| --- | --- | --- | --- |
| Tab first screen (tab bar visible) | `statusBarsPadding()` | `routeeNavBarPadding()` | `imePadding()` + scroll |
| Other screens (no tab bar) | `statusBarsPadding()` | `navigationBarsPadding()` | `imePadding()` + scroll |

```kotlin
// 탭 첫 화면: 탭 Scene이 탭 바와 내비게이션 바 인셋을 이미 소비함. navigationBarsPadding() 금지
LazyColumn(contentPadding = PaddingValues(bottom = routeeNavBarPadding(RouteeTheme.spacing.Spacing20))) { ... } // 목록: 끝에만
Box(Modifier.padding(bottom = routeeNavBarPadding(RouteeTheme.spacing.Spacing16)))                            // 하단 고정 버튼: 탭 바 위로

// 입력 화면: manifest는 adjustResize. imePadding은 스크롤과 함께
Column(Modifier.fillMaxSize().statusBarsPadding().imePadding().verticalScroll(rememberScrollState())) {
    // 내용
    Spacer(Modifier.height(routeeNavBarPadding())) // 탭 아래면 스크롤 안쪽 끝에
}

// bringIntoViewOnFocus, preventCursorScroll은 imePadding + 스크롤로 부족할 때만
```
