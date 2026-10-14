# Screen

| Part | Holds |
| --- | --- |
| `XxxRoute` | ViewModel, state collection, side effects. Navigation callbacks only |
| `XxxScreen` | `private`. `uiState` + lambdas. No ViewModel |
| `XxxSideEffect` | One-off events (toast, navigate, dialog). `<screen>/model/` next to UiState |

```kotlin
@Composable
internal fun FeatureRoute(
    onItemClick: (Long) -> Unit, // 이동은 entry builder가 결정
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    viewModel.sideEffect.collectSideEffect { sideEffect -> // 화면이 보일 때(STARTED)만 수집
        when (sideEffect) {
            is FeatureSideEffect.NavigateToDetail -> onItemClick(sideEffect.itemId)
            FeatureSideEffect.ShowDeleteToast -> { /* 토스트 */ }
        }
    }

    FeatureScreen(
        uiState = uiState,
        onItemClick = viewModel::onItemClick,
        onDeleteClick = viewModel::onDeleteClick,
    )
}

@Composable
private fun FeatureScreen(
    uiState: FeatureUiState,
    onItemClick: (Long) -> Unit, // 기본은 이벤트마다 람다
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) { ... }

// 이벤트가 많으면 선택: onEvent 하나로 묶기
// private fun FeatureScreen(uiState: FeatureUiState, onEvent: (FeatureEvent) -> Unit, modifier: Modifier = Modifier)
```

## Side Effect

```kotlin
// <screen>/model/FeatureSideEffect.kt
internal sealed interface FeatureSideEffect {
    data class NavigateToDetail(val itemId: Long) : FeatureSideEffect
    data object ShowDeleteToast : FeatureSideEffect
}

// ViewModel: Channel이라 수집이 잠깐 멈춰도(회전, 백그라운드) 이벤트가 사라지지 않음
private val _sideEffect = Channel<FeatureSideEffect>(Channel.BUFFERED)
val sideEffect = _sideEffect.receiveAsFlow()

fun onDeleteClick(itemId: Long) {
    viewModelScope.launch { _sideEffect.send(FeatureSideEffect.ShowDeleteToast) }
}
```

| Don't | Do |
| --- | --- |
| `MutableSharedFlow(replay = 0)` for side effects | `Channel(BUFFERED).receiveAsFlow()` |
| `LaunchedEffect(Unit) { flow.collect { } }` | `collectSideEffect { }` (`core:ui`) |
| Toast/navigation state in UiState | `XxxSideEffect` |
| Dialog visibility as a side effect | `isXxxDialogVisible` in UiState ([navigation](navigation.md)) |

State: [ui-state](ui-state.md)
