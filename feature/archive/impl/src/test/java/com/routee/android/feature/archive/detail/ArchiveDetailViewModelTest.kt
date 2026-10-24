package com.routee.android.feature.archive.detail

import com.routee.android.core.data.archive.model.ArchiveDetail
import com.routee.android.core.data.archive.model.ArchivePhoto
import com.routee.android.core.data.archive.repository.ArchiveDetailRepository
import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.recap.model.RecapRoute
import com.routee.android.core.data.recap.model.TimelineMarker
import com.routee.android.core.data.recap.model.TrackPoint
import com.routee.android.core.ui.UiState
import com.routee.android.feature.archive.MainDispatcherRule
import com.routee.android.feature.archive.api.ArchiveDetailNavKey
import com.routee.android.feature.archive.detail.model.ArchiveDetailRecord
import com.routee.android.feature.archive.detail.model.ArchiveDetailSideEffect
import com.routee.android.feature.archive.detail.model.toArchiveDetailRecord
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveDetailViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun titleIsTrimmedAndUnchangedBlankOrTooLongTitlesDoNotReachServer() = runTest {
        val repository = FakeRepository().apply { fail = false }
        val viewModel = ArchiveDetailViewModel(ArchiveDetailNavKey(42), repository, dagger.Lazy { error("Unused") })

        listOf("  정상 산행  ", "정상 산행", " ", "가".repeat(17)).forEach { title ->
            viewModel.onTitleEditingChange(true)
            viewModel.onTitleEditFinish(title)
            runCurrent()
        }
        assertEquals(listOf(42L to "정상 산행"), repository.updatedTitles)
        assertEquals("정상 산행", (viewModel.uiState.value.record as UiState.Success).data.title)
    }

    @Test
    fun failedMutationKeepsRecordAndDialogAndSuccessfulDeleteClosesIt() = runTest {
        val repository = FakeRepository().apply {
            fail = false
            mutationFails = true
        }
        val viewModel = ArchiveDetailViewModel(ArchiveDetailNavKey(42), repository, dagger.Lazy { error("Unused") })
        viewModel.onTitleEditingChange(true)
        viewModel.onTitleEditFinish("새 제목")
        runCurrent()
        assertEquals(ArchiveDetailSideEffect.TitleSaveFailed, viewModel.sideEffect.first())
        assertEquals("북악산", (viewModel.uiState.value.record as UiState.Success).data.title)

        viewModel.onDeleteDialogChange(true)
        viewModel.onDeleteConfirm()
        runCurrent()
        assertEquals(ArchiveDetailSideEffect.DeleteFailed, viewModel.sideEffect.first())
        assertTrue(viewModel.uiState.value.isDeleteDialogVisible)
        assertTrue(!viewModel.uiState.value.isSaving)

        repository.mutationFails = false
        viewModel.onDeleteConfirm()
        runCurrent()
        assertEquals(ArchiveDetailSideEffect.Deleted, viewModel.sideEffect.first())
        assertTrue(!viewModel.uiState.value.isDeleteDialogVisible)
        assertEquals(listOf(42L, 42L), repository.deletedIds)
    }

    @Test
    fun concurrentMutationIsBlockedAndCompletionAllowsNextRequest() = runTest {
        val repository = FakeRepository().apply {
            fail = false
            gate = CompletableDeferred()
        }
        val viewModel = ArchiveDetailViewModel(ArchiveDetailNavKey(42), repository, dagger.Lazy { error("Unused") })
        viewModel.onTitleEditingChange(true)
        viewModel.onTitleEditFinish("새 제목")
        runCurrent()
        assertTrue(viewModel.uiState.value.isSaving)
        viewModel.onDeleteConfirm()
        assertTrue(repository.deletedIds.isEmpty())

        repository.gate!!.complete(Unit)
        runCurrent()
        assertTrue(!viewModel.uiState.value.isSaving)
        viewModel.onDeleteConfirm()
        runCurrent()
        assertEquals(listOf(42L), repository.deletedIds)
    }

    @Test
    fun cancellingUiCallerDoesNotCancelTitleRequest() = runTest {
        val repository = FakeRepository().apply {
            fail = false
            gate = CompletableDeferred()
        }
        val viewModel = ArchiveDetailViewModel(ArchiveDetailNavKey(42), repository, dagger.Lazy { error("Unused") })
        val caller = launch {
            viewModel.onTitleEditingChange(true)
            viewModel.onTitleEditFinish("새 제목")
            awaitCancellation()
        }
        runCurrent()
        caller.cancelAndJoin()
        assertTrue(viewModel.uiState.value.isSaving)

        repository.gate!!.complete(Unit)
        runCurrent()
        assertEquals("새 제목", (viewModel.uiState.value.record as UiState.Success).data.title)
        assertEquals(ArchiveDetailSideEffect.RecordChanged, viewModel.sideEffect.first())
        assertTrue(!viewModel.uiState.value.isSaving)
    }

    @Test
    fun failedTitleSaveBlocksBackAndSuccessfulSaveCanOpenDeleteDialog() = runTest {
        val repository = FakeRepository().apply {
            fail = false
            mutationFails = true
        }
        val viewModel = ArchiveDetailViewModel(ArchiveDetailNavKey(42), repository, dagger.Lazy { error("Unused") })
        viewModel.onTitleEditingChange(true)
        viewModel.onBackClick("새 제목")
        runCurrent()
        assertEquals(ArchiveDetailSideEffect.TitleSaveFailed, viewModel.sideEffect.first())

        repository.mutationFails = false
        viewModel.onTitleEditingChange(true)
        viewModel.onDeleteClick("다시 수정")
        runCurrent()
        assertEquals(ArchiveDetailSideEffect.RecordChanged, viewModel.sideEffect.first())
        assertTrue(viewModel.uiState.value.isDeleteDialogVisible)

        viewModel.onBackClick("다시 수정")
        assertEquals(ArchiveDetailSideEffect.NavigateBack, viewModel.sideEffect.first())
    }

    @Test
    fun formatsServerIntegersAndKeepsTrackOrderAndPhotoMarkers() {
        val record = detail().toArchiveDetailRecord()
        assertEquals("15.53", record.distance)
        assertEquals("03:20", record.duration)
        assertEquals("2132", record.maxElevation)
        assertEquals("2026.03.12", record.date)
        assertEquals(listOf(37.0, 38.0), record.track.map { it.latitude })
        assertEquals("thumbnail", record.markers.single().imageUrl)
        assertEquals(null, record.photos.single().location)
        assertEquals(Instant.parse("2026-03-12T03:00:00Z"), record.photos.single().createdAt)
        assertEquals(listOf("창의문"), record.routeNames)
    }

    @Test
    fun photosAndRoutesMayBothBeEmptyWithoutMakingRecordEmpty() {
        val record = detail().copy(photos = emptyList(), routes = emptyList()).toArchiveDetailRecord()
        assertTrue(record.photos.isEmpty())
        assertTrue(record.routeNames.isEmpty())
        assertTrue(record.track.isNotEmpty())
    }

    @Test
    fun loadsNavigationActivityIdAndRecoversFromFailure() = runTest {
        val repository = FakeRepository()
        val viewModel = ArchiveDetailViewModel(ArchiveDetailNavKey(42), repository, dagger.Lazy { error("Unused") })
        assertTrue(viewModel.uiState.value.record is UiState.Failure)

        repository.fail = false
        viewModel.onRetryClick()

        assertEquals(listOf(42L, 42L), repository.activityIds)
        assertEquals("북악산", (viewModel.uiState.value.record as UiState.Success<ArchiveDetailRecord>).data.title)
    }

    private class FakeRepository : ArchiveDetailRepository {
        var mutationFails = false
        var gate: CompletableDeferred<Unit>? = null
        val updatedTitles = mutableListOf<Pair<Long, String>>()
        val deletedIds = mutableListOf<Long>()
        override suspend fun updateTitle(activityId: Long, title: String): Result<Unit> {
            updatedTitles += activityId to title
            gate?.await()
            return if (mutationFails) Result.failure(IOException()) else Result.success(Unit)
        }
        override suspend fun deleteActivity(activityId: Long): Result<Unit> {
            deletedIds += activityId
            return if (mutationFails) Result.failure(IOException()) else Result.success(Unit)
        }

        var fail = true
        val activityIds = mutableListOf<Long>()
        override suspend fun getDetail(activityId: Long): Result<ArchiveDetail> {
            activityIds += activityId
            return if (fail) Result.failure(IOException()) else Result.success(detail())
        }

        override suspend fun getPhotos(activityId: Long): Result<List<ArchivePhoto>> = error("Unused")
    }

    companion object {
        private fun detail() = ArchiveDetail(
            "북악산",
            LocalDate.of(2026, 3, 12),
            15530,
            12000,
            2132,
            ActivityTrack(
                listOf(TrackPoint(38.0, 127.0, 1.0, 2), TrackPoint(37.0, 127.0, 1.0, 1)),
                listOf(TimelineMarker(5, "thumbnail", 37.0, 127.0, 1)),
            ),
            listOf(ArchivePhoto(5, null, "photo", Instant.parse("2026-03-12T03:00:00Z"))),
            listOf(RecapRoute(1, "창의문")),
        )
    }
}
