# Compose

## Component

| Item | Rule |
| --- | --- |
| Location | 1 feature → `feature/<x>/impl/.../component/<screen>/`, `internal`. 2+ features → `core/designsystem/.../component/`, `Routee` prefix ([modularization](../architecture/modularization.md)) |
| Name | Figma name in PascalCase. Add the shape when it helps (`Card`, `Chip`, `Bar`). Use the Android term when Figma differs (`modal` → `Dialog`), Figma name in the `//` comment |
| Fixed content | Take values, not a slot, when Figma never changes the items |
| List parameter | `List`. `ImmutableList` only for lists in UiState ([ui-state](../architecture/ui-state.md)) |
| Screen | Route, Screen, events ([screen](../architecture/screen.md)) |

```kotlin
// feature_card_android            ← Figma 이름. designsystem은 주석, feature는 KDoc 괄호
private val ItemSpacing = 4.dp     // 토큰에 없는 값만 private 상수

/**
 * 기능 카드
 *
 * @param title    제목
 * @param value    화면에 보일 문자열 (예: "15.53")
 * @param onClick  탭 시 호출
 * @param badge    오른쪽 뱃지. null 이면 숨김
 */
@Composable
fun RouteeFeatureCard(
    title: String,                 // 필수 → modifier → 선택 → content 순서
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier, // modifier는 @param 생략
    badge: String? = null,
)
```

| KDoc | Rule |
| --- | --- |
| Title | Short noun phrase. Feature module: `설명 (figma_name)` |
| Behavior | Aligned `@param` lines with short phrases (`탭 시 호출`, `null 이면 숨김`, `true 이면 ~ 표시`) |
| Skip | `modifier`, width/size notes (`폭은 [modifier]로 정한다`) |

## Design Tokens

| Value | Use |
| --- | --- |
| Color | `RouteeTheme.colors` |
| Text style | `RouteeTheme.typography` |
| Spacing | `RouteeTheme.spacing` |
| Corner radius | `RouteeTheme.radius` (`Full` for pills) |
| Icon size, Figma-only gap | `private val Xxx = 28.dp` in the file |
| Drawable size mismatch | `Modifier.size()`. Don't edit the drawable |

## Text

| Text | Use | Example |
| --- | --- | --- |
| Must keep meaning, one line in a fixed cell | `RouteeAutoSizeText` | Numbers in a card, short labels |
| Must keep meaning, free height | `Text` (wrap) | Guide text, dialog body |
| Can be cut | `Text` + `maxLines = 1` + `TextOverflow.Ellipsis` | Chip, button, address, tab |

```kotlin
RouteeAutoSizeText(text = value, style = RouteeTheme.typography.displaySb28) // 넘치면 12sp까지 줄임. 커지지는 않음
// 200% 글자에서 겹치거나 사라지는 게 없으면 통과
```

## Preview

| Target    | Annotation                                                                 |
|-----------|----------------------------------------------------------------------------|
| Component | `@PreviewFontSizes` (default + 200% font)                                  |
| Screen    | `@PreviewScreenSizes` (Compose built-in: phone, foldable, tablet, desktop) |

```kotlin
// 컴포넌트: @Preview 대신. 폰트 크기 확인용
@PreviewFontSizes
@Composable
private fun FeatureButtonPreview() {
    RouteePreviewTheme {
        Column { // 상태가 여러 개면 한 Preview에 나열
            FeatureButton(isSelected = false, onClick = {})
            FeatureButton(isSelected = true, onClick = {})
        }
    }
}

// 화면: Route가 아닌 Screen을 미리보기. 필요시 사용
@PreviewScreenSizes
@Composable
private fun FeatureScreenPreview() {
    RouteePreviewTheme {
        FeatureScreen(uiState = FeatureUiState())
    }
}
```

| Item        | Rule                                               |
|-------------|----------------------------------------------------|
| Function    | `private`, `<Component>Preview`, `<Screen>Preview` |
| Annotations | `core/designsystem/.../preview/`                   |
| Theme       | `RouteeTheme`, or `RouteePreviewTheme` when the app background helps |
