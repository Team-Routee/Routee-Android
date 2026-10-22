package com.routee.android.feature.recordedit.complete

import com.routee.android.feature.recordedit.MainDispatcherRule
import com.routee.android.feature.recordedit.api.RecapCompleteNavKey
import com.routee.android.feature.recordedit.complete.model.RecapCompleteSideEffect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RecapCompleteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * GIVEN: 갤러리 저장이 성공할 때
     * WHEN: 다운로드를 누르면
     * THEN: 저장 완료 토스트를 띄운다
     */
    @Test
    fun `갤러리 저장에 성공하면 저장 완료 토스트를 띄운다`() = runTest {
        val viewModel = viewModel(FakeRecapImageRepository())

        viewModel.onDownloadClick()

        assertEquals(RecapCompleteSideEffect.ShowSaveSuccessToast, viewModel.sideEffect.first())
    }

    /**
     * GIVEN: 갤러리 저장이 실패할 때
     * WHEN: 다운로드를 누르면
     * THEN: 저장 실패 토스트를 띄운다
     */
    @Test
    fun `갤러리 저장에 실패하면 저장 실패 토스트를 띄운다`() = runTest {
        val viewModel =
            viewModel(FakeRecapImageRepository(saveToGalleryResult = Result.failure(IllegalStateException())))

        viewModel.onDownloadClick()

        assertEquals(RecapCompleteSideEffect.ShowSaveFailureToast, viewModel.sideEffect.first())
    }

    private fun viewModel(repository: RecapImageRepository) =
        RecapCompleteViewModel(RecapCompleteNavKey(FakeRecapImageRepository.IMAGE_PATH), repository)
}
