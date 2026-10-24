package com.routee.android.feature.archive.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.archive.repository.ArchiveDetailRepository
import com.routee.android.core.data.photo.GalleryImageStore
import com.routee.android.core.navigation.util.NavKeyViewModelFactory
import com.routee.android.core.ui.UiState
import com.routee.android.feature.archive.api.ArchiveDetailNavKey
import com.routee.android.feature.archive.detail.model.ArchiveDetailSideEffect
import com.routee.android.feature.archive.detail.model.ArchiveDetailUiState
import com.routee.android.feature.archive.detail.model.toArchiveDetailRecord
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = ArchiveDetailViewModel.Factory::class)
internal class ArchiveDetailViewModel @AssistedInject constructor(
    @Assisted private val key: ArchiveDetailNavKey,
    private val repository: ArchiveDetailRepository,
    private val galleryImageStore: Lazy<GalleryImageStore>,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArchiveDetailUiState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = Channel<ArchiveDetailSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()
    private var loadJob: Job? = null

    init {
        onRetryClick()
    }

    suspend fun savePhoto(imageUrl: String, capturedAtEpochMillis: Long): Result<Unit> =
        galleryImageStore.get().save(imageUrl, capturedAtEpochMillis).onFailure { Timber.e(it) }

    fun onTitleEditingChange(editing: Boolean) {
        if (!_uiState.value.isSaving) _uiState.update { it.copy(isTitleEditing = editing) }
    }

    fun onDeleteDialogChange(visible: Boolean) {
        if (!_uiState.value.isSaving) _uiState.update { it.copy(isDeleteDialogVisible = visible) }
    }

    fun onTitleEditFinish(title: String) = finishTitleEdit(title)

    fun onBackClick(title: String) = finishTitleEdit(title) {
        _sideEffect.trySend(ArchiveDetailSideEffect.NavigateBack)
    }

    fun onDeleteClick(title: String) = finishTitleEdit(title) {
        _uiState.update { it.copy(isDeleteDialogVisible = true) }
    }

    private fun finishTitleEdit(title: String, onFinished: () -> Unit = {}) {
        if (_uiState.value.isSaving) return
        if (!_uiState.value.isTitleEditing) {
            onFinished()
            return
        }
        val record = (_uiState.value.record as? UiState.Success)?.data ?: return
        val normalized = title.trim()
        if (normalized.isEmpty() || normalized.length > 16) return
        _uiState.update { it.copy(isTitleEditing = false) }
        if (normalized == record.title) {
            onFinished()
            return
        }
        mutate(ArchiveDetailSideEffect.TitleSaveFailed, { repository.updateTitle(key.activityId, normalized) }) {
            _uiState.update { it.copy(record = UiState.Success(record.copy(title = normalized))) }
            _sideEffect.send(ArchiveDetailSideEffect.RecordChanged)
            onFinished()
        }
    }

    fun onDeleteConfirm() = mutate(ArchiveDetailSideEffect.DeleteFailed, {
        repository.deleteActivity(key.activityId)
    }) {
        _uiState.update { it.copy(isDeleteDialogVisible = false) }
        _sideEffect.send(ArchiveDetailSideEffect.Deleted)
    }

    private fun mutate(
        failure: ArchiveDetailSideEffect,
        request: suspend () -> Result<Unit>,
        onSuccess: suspend () -> Unit,
    ) {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                request().onSuccess { onSuccess() }.onFailure {
                    Timber.e(it)
                    _sideEffect.send(failure)
                }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
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
