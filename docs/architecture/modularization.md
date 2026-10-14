# Modularization

| Module | Contains |
| --- | --- |
| `app` | `MainActivity`, `NavDisplay`, tab bar, entry registration |
| `core:common` | Kotlin utilities (`suspendRunCatching`) |
| `core:designsystem` | Tokens, `Routee*` components, Modifier extensions, drawables |
| `core:navigation` | `RouteeNavigator`, `viewModelEntry` |
| `core:ui` | `UiState` and other shared UI code |
| `core:data` | Repository, DataSource, DTO, model |
| `core:network` | Retrofit, `ApiResponseHandler`, `ApiException` |
| `feature:<x>:api` | `NavKey`, result types |
| `feature:<x>:impl` | Route, Screen, ViewModel, entry builder |

Features: `onboarding`, `workout`, `record-edit`, `timeline`, `archive`, `setting`

## Dependencies

```kotlin
// feature/record-edit/impl/build.gradle.kts
plugins { alias(libs.plugins.routee.android.feature.impl) } // Android, Compose, Hilt, ktlint 설정은 플러그인이 함

dependencies {
    implementation(projects.feature.recordEdit.api) // 자기 api는 직접 선언
    implementation(projects.feature.workout.api)    // 다른 feature는 api만. impl 의존 금지
}
// 라이브러리 추가는 팀에 먼저 묻고 libs.versions.toml에
```

## File Locations

| Code | Path |
| --- | --- |
| Screen, ViewModel, screen-only component | `feature/<x>/impl/.../<screen>/` (`main/`, `editor/`) |
| UiState | `feature/<x>/impl/.../<screen>/model/` |
| Component shared within a feature | `feature/<x>/impl/.../component/` |
| Entry builder | `feature/<x>/impl/.../navigation/<X>EntryBuilder.kt` |
| Component used in 2+ features | `core/designsystem/.../component/` |
| Modifier extension | `core/designsystem/.../extension/ModifierExt.kt` |
| Feature strings | `feature/<x>/impl/src/main/res/values/strings.xml` |
| Repository, DTO, model | `core/data/.../<domain>/` |

New module: team decision first → `settings.gradle.kts` → `app/build.gradle.kts` → `RouteeApp`
