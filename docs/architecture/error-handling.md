# Error Handling

| Where | What it does with a failure | Logs |
| --- | --- | --- |
| Retrofit service | Throws | No |
| Repository | Wraps it in `Result`, maps server codes to domain errors | No |
| ViewModel | Turns it into UiState | Yes |

## Repository

```kotlin
override suspend fun register(...): Result<Unit> =
    apiResponseHandler.safeUnitApiCall { authService.register(request) } // 실패는 항상 ApiException
        .mapApiException(::mapAuthException) // 서버 코드 → 도메인 에러(AuthException). ViewModel은 서버 코드 문자열을 모름

suspendRunCatching { tokenLocalDataSource.saveTokens(accessToken, refreshToken) } // API가 아닌 suspend 작업. runCatching은 취소를 삼킴
```

## ViewModel

```kotlin
fun loadRecap(id: Long) {
    viewModelScope.launch {
        recapRepository.getRecap(id)
            .onSuccess { recap -> _uiState.update { it.copy(recap = UiState.Success(recap)) } }
            .onFailure { e ->
                Timber.e(e) // 에러 로그는 ViewModel에서 한 번만
                _uiState.update { it.copy(recap = UiState.Failure(e)) }
            }
    }
}
```
