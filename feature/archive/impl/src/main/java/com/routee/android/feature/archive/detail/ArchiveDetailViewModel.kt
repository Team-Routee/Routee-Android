package com.routee.android.feature.archive.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.archive.repository.ArchiveDetailRepository
import com.routee.android.core.navigation.util.NavKeyViewModelFactory
import com.routee.android.core.ui.UiState
import com.routee.android.feature.archive.api.ArchiveDetailNavKey
import com.routee.android.feature.archive.detail.model.ArchiveDetailUiState
import com.routee.android.feature.archive.detail.model.toArchiveDetailRecord
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = ArchiveDetailViewModel.Factory::class)
internal class ArchiveDetailViewModel @AssistedInject constructor(
    @Assisted private val key: ArchiveDetailNavKey,
    private val repository: ArchiveDetailRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArchiveDetailUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        onRetryClick()
    }

    fun onTitleEditingChange(editing: Boolean) {
        _uiState.update { it.copy(isTitleEditing = editing) }
    }

    fun onDeleteDialogChange(visible: Boolean) {
        _uiState.update { it.copy(isDeleteDialogVisible = visible) }
    }

    fun onRetryClick() {
        loadJob?.cancel()
        _uiState.update { it.copy(record = UiState.Loading) }
        loadJob = viewModelScope.launch {
            repository.getDetail(key.activityId)
                .onSuccess { detail ->
                    _uiState.update { it.copy(record = UiState.Success(detail.toArchiveDetailRecord())) }
                }
                .onFailure { error ->
                    Timber.e(error)
                    _uiState.update { it.copy(record = UiState.Failure(error)) }
                }
        }
    }

    @AssistedFactory
    interface Factory : NavKeyViewModelFactory<ArchiveDetailNavKey, ArchiveDetailViewModel>
}
