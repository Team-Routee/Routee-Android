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
import com.routee.android.feature.archive.detail.model.toArchiveDetailRecord
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ArchiveDetailViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

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
        var fail = true
        val activityIds = mutableListOf<Long>()
        override suspend fun getDetail(activityId: Long): Result<ArchiveDetail> {
            activityIds += activityId
            return if (fail) Result.failure(IOException()) else Result.success(detail())
        }
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
            listOf(ArchivePhoto(5, " ", "photo", Instant.parse("2026-03-12T03:00:00Z"))),
            listOf(RecapRoute(1, "창의문")),
        )
    }
}
