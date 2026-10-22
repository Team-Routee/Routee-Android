package com.routee.android.feature.recordedit.complete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.navigation.util.NavKeyViewModelFactory
import com.routee.android.feature.recordedit.api.RecapCompleteNavKey
import com.routee.android.feature.recordedit.complete.model.RecapCompleteSideEffect
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = RecapCompleteViewModel.Factory::class)
internal class RecapCompleteViewModel @AssistedInject constructor(
    @Assisted private val key: RecapCompleteNavKey,
    private val recapImageRepository: RecapImageRepository,
) : ViewModel() {
    private val _sideEffect = Channel<RecapCompleteSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    val imagePath: String = key.imagePath

    private var isSaving = false

    fun onBackClick() {
        sendSideEffect(RecapCompleteSideEffect.NavigateBack)
    }

    fun onCompleteClick() {
        sendSideEffect(RecapCompleteSideEffect.NavigateToHome)
    }

    fun onDownloadClick() {
        saveToGallery()
    }

    fun onShareClick() {
        sendSideEffect(RecapCompleteSideEffect.ShareImage(recapImageRepository.shareUri(key.imagePath)))
    }

    private fun saveToGallery() {
        if (isSaving) return
        isSaving = true
        viewModelScope.launch {
            recapImageRepository.saveToGallery(key.imagePath)
                .onSuccess { _sideEffect.send(RecapCompleteSideEffect.ShowSaveSuccessToast) }
                .onFailure { e ->
                    Timber.e(e)
                    _sideEffect.send(RecapCompleteSideEffect.ShowSaveFailureToast)
                }
            isSaving = false
        }
    }

    private fun sendSideEffect(sideEffect: RecapCompleteSideEffect) {
        viewModelScope.launch { _sideEffect.send(sideEffect) }
    }

    @AssistedFactory
    interface Factory : NavKeyViewModelFactory<RecapCompleteNavKey, RecapCompleteViewModel>
}
