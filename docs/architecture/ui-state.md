# UI State

```kotlin
// 화면 상태는 화면마다 data class로 만든다
@Immutable
internal data class FeatureUiState(
    val items: UiState<ImmutableList<Item>> = UiState.Loading, // 필요하면 core:ui의 UiState<T>를 필드로. 안 맞으면 화면에서 직접 sealed로 커스텀
    val isDeleteDialogVisible: Boolean = false,
)

// ViewModel
fun loadItems() {
    viewModelScope.launch {
        itemRepository.getItems()
            .onSuccess { items ->
                _uiState.update { it.copy(items = if (items.isEmpty()) UiState.Empty else UiState.Success(items.toImmutableList())) }
            }
            .onFailure { e -> _uiState.update { it.copy(items = UiState.Failure(e)) } }
    }
}

// Screen
when (val items = uiState.items) {
    UiState.Loading -> { /* 로딩 */ }
    UiState.Empty -> { /* 빈 화면 */ }
    is UiState.Success -> ItemList(items = items.data)
    is UiState.Failure -> { /* 표시 방식은 기획을 따름 */ }
}
```
