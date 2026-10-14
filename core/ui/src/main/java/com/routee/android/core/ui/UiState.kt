package com.routee.android.core.ui

/**
 * 불러오는 데이터의 상태. 화면 상태의 필드로 필요할 때 쓴다
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>

    data object Empty : UiState<Nothing>

    data class Success<T>(val data: T) : UiState<T>

    data class Failure(val throwable: Throwable) : UiState<Nothing>
}
