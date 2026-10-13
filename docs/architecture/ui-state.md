# UI State

```kotlin
// 화면 상태는 화면마다 data class로 만든다
@Immutable
internal data class ArchiveUiState(
    val recaps: UiState<ImmutableList<Recap>> = UiState.Loading, // 필요하면 core:ui의 UiState<T>를 필드로. 안 맞으면 화면에서 직접 sealed로 커스텀
    val isDeleteDialogVisible: Boolean = false,
)

// ViewModel
fun loadRecaps() {
    viewModelScope.launch {
        recapRepository.getRecaps()
            .onSuccess { recaps ->
                _uiState.update { it.copy(recaps = if (recaps.isEmpty()) UiState.Empty else UiState.Success(recaps.toImmutableList())) }
            }
            .onFailure { e -> _uiState.update { it.copy(recaps = UiState.Failure(e)) } }
    }
}

// Screen
when (val recaps = uiState.recaps) {
    UiState.Loading -> { /* 로딩 */ }
    UiState.Empty -> { /* 빈 화면 */ }
    is UiState.Success -> RecapList(recaps = recaps.data)
    is UiState.Failure -> { /* 표시 방식은 기획을 따름 */ }
}
```
