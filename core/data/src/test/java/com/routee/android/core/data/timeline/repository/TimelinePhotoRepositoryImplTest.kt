package com.routee.android.core.data.timeline.repository

import com.routee.android.core.data.timeline.model.TimelinePhotoStatus
import com.routee.android.core.data.timeline.model.TimelinePhotoUpload
import com.routee.android.core.data.timeline.remote.dto.request.CreateTimelineRequest
import com.routee.android.core.data.timeline.remote.dto.response.TimelineCreateResponse
import com.routee.android.core.data.timeline.remote.service.TimelinePhotoService
import com.routee.android.core.data.upload.PresignedImageUploader
import com.routee.android.core.data.upload.remote.dto.request.ImageUrlRequest
import com.routee.android.core.data.upload.remote.dto.response.ImageUrlResponse
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.network.model.BaseResponse
import com.routee.android.core.network.util.ApiResponseHandler
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TimelinePhotoRepositoryImplTest {
    @get:Rule val folder = TemporaryFolder()

    private val putCount = AtomicInteger()
    private lateinit var server: HttpServer

    @Before
    fun setUp() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/upload") { exchange ->
                exchange.requestBody.use { it.readBytes() }
                putCount.incrementAndGet()
                exchange.sendResponseHeaders(200, -1)
                exchange.close()
            }
            createContext("/forbidden") { exchange ->
                exchange.sendResponseHeaders(403, -1)
                exchange.close()
            }
            start()
        }
    }

    @After
    fun tearDown() = server.stop(0)

    @Test
    fun retryRegistersOnlyFailedPhotoWithoutUploadingAgain() = runTest {
        val service = FakeService("http://127.0.0.1:${server.address.port}/upload")
        val repository = TimelinePhotoRepositoryImpl(
            service = service,
            uploader = PresignedImageUploader(OkHttpClient()),
            apiResponseHandler = ApiResponseHandler(Json),
        )
        val photos = listOf(photo("first.jpg"), photo("second.jpg"))

        val first = repository.uploadPhotos(1, photos)
        assertTrue(first[0].result.isFailure)
        assertTrue(first[1].result.isSuccess)

        val retry = repository.uploadPhotos(1, photos)
        assertTrue(retry.all { it.result.isSuccess })
        assertEquals(2, service.urlRequests)
        assertEquals(3, service.registerRequests)
        assertEquals(2, putCount.get())
        assertEquals(listOf("first.jpg", "second.jpg"), retry.map { it.clientId })
    }

    @Test
    fun uploadHttpFailureIsNotMappedToNetworkFailure() = runTest {
        val uploader = PresignedImageUploader(OkHttpClient())
        val file = File(folder.root, "photo.jpg").apply { writeText("photo") }

        val result = ApiResponseHandler(Json).safeUnitApiCall {
            uploader.upload("http://127.0.0.1:${server.address.port}/forbidden", file)
        }

        assertTrue(result.exceptionOrNull() is ApiException.Http)
        assertEquals(403, (result.exceptionOrNull() as ApiException.Http).statusCode)
    }

    @Test
    fun clearUploadStateAllowsCompletedActivityToUploadAgain() = runTest {
        val service = FakeService("http://127.0.0.1:${server.address.port}/upload", failFirstRegistration = false)
        val repository = TimelinePhotoRepositoryImpl(
            service = service,
            uploader = PresignedImageUploader(OkHttpClient()),
            apiResponseHandler = ApiResponseHandler(Json),
        )
        val photos = listOf(photo("photo.jpg"))

        repository.uploadPhotos(1, photos)
        repository.clearUploadState(1)
        repository.uploadPhotos(1, photos)

        assertEquals(2, service.urlRequests)
        assertEquals(2, service.registerRequests)
        assertEquals(2, putCount.get())
    }

    private fun photo(name: String): TimelinePhotoUpload = TimelinePhotoUpload(
        clientId = name,
        file = File(folder.root, name).apply { writeText(name) },
        capturedAtEpochMillis = 1_700_000_000_000,
        title = "",
        trackPointIndex = 1,
        longitude = 127.0,
        latitude = 37.0,
        elevation = 100,
        pointIndex = 1,
        status = TimelinePhotoStatus.SUCCESSFUL_CREATED,
    )

    private class FakeService(
        private val uploadUrl: String,
        private val failFirstRegistration: Boolean = true,
    ) : TimelinePhotoService {
        private val urlRequestCount = AtomicInteger()
        private val registerRequestCount = AtomicInteger()
        val urlRequests get() = urlRequestCount.get()
        val registerRequests get() = registerRequestCount.get()
        private var firstRegistrationFailed = false

        override suspend fun createImageUrl(
            activityId: Long,
            request: ImageUrlRequest,
        ): BaseResponse<ImageUrlResponse> {
            urlRequestCount.incrementAndGet()
            return response(ImageUrlResponse(uploadUrl, request.fileName))
        }

        override suspend fun createTimeline(
            activityId: Long,
            request: CreateTimelineRequest,
        ): BaseResponse<TimelineCreateResponse> {
            val requestCount = registerRequestCount.incrementAndGet()
            if (failFirstRegistration && request.timelineImageObjectKey == "first.jpg" && !firstRegistrationFailed) {
                firstRegistrationFailed = true
                throw java.io.IOException("temporary failure")
            }
            return response(TimelineCreateResponse(requestCount.toLong()))
        }

        private fun <T> response(data: T) = BaseResponse(status = 200, code = "SUCCESS", message = "OK", data = data)
    }
}
