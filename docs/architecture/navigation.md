# Navigation

## NavKey

```kotlin
// feature/<x>/api
@Serializable
data object LoginNavKey : NavKey

@Serializable
data class RecapDetailNavKey(val activityId: Long) : NavKey
```

## Moving

| Don't | Do |
| --- | --- |
| `backStack.add(key)` | `navigator.navigateTo(key)` |
| `backStack.removeLast()` | `navigator.navigateBack()` |
| `backStack.clear()` | `navigator.clearAndNavigateTo(key)` |
| Dialog or bottom sheet as a `NavKey` | `isXxxDialogVisible` in UiState |
| Shared singleton for results | `ResultEventBus` between nav entries |
| Switching tabs from a screen | Tab bar only |

## Entry Builder

```kotlin
// feature/<x>/impl/.../navigation/<X>EntryBuilder.kt
fun EntryProviderScope<NavKey>.onboardingEntries(navigator: RouteeNavigator) {
    entry<LoginNavKey> {
        LoginRoute(onLoginClick = { navigator.navigateTo(TermsNavKey) }) // 이동 결정은 여기서만. Route는 콜백만 받음
    }
    entry<TermsNavKey> {
        TermsRoute(onBackClick = navigator::navigateBack)
    }
}

// app/.../RouteeApp.kt
entryProvider {
    onboardingEntries(navigator) // 새 feature는 여기에 추가
}
```

## ViewModel with Arguments

```kotlin
@HiltViewModel(assistedFactory = RecapDetailViewModel.Factory::class)
class RecapDetailViewModel @AssistedInject constructor(
    @Assisted private val key: RecapDetailNavKey,
) : ViewModel() {
    @AssistedFactory
    interface Factory : NavKeyViewModelFactory<RecapDetailNavKey, RecapDetailViewModel>
}

viewModelEntry<RecapDetailNavKey, RecapDetailViewModel, RecapDetailViewModel.Factory> { _, viewModel ->
    RecapDetailRoute(viewModel = viewModel, onBackClick = navigator::navigateBack)
}
```

## RouteeApp

```kotlin
entryDecorators = listOf(
    rememberSaveableStateHolderNavEntryDecorator(), // 순서 중요
    rememberViewModelStoreNavEntryDecorator(),
    rememberResultEventBusNavEntryDecorator(),
)
sceneDecoratorStrategies = listOf(rememberRouteeTabSceneDecorator(backStack, navigator)) // 탭 첫 화면에만 탭 바
```
