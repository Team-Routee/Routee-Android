package com.routee.android.core.data.upload

import com.routee.android.core.data.di.PresignedUpload
import com.routee.android.core.network.exception.ApiException
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response

internal class PresignedImageUploader @Inject constructor(
    @PresignedUpload private val client: OkHttpClient,
) {
    suspend fun upload(url: String, file: File) {
        val mediaType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "heic" -> "image/heic"
            else -> throw IllegalArgumentException("지원하지 않는 사진 형식")
        }
        val request = Request.Builder()
            .url(url)
            .put(file.asRequestBody(mediaType.toMediaType()))
            .build()
        client.newCall(request).await()
    }
}

private suspend fun Call.await() = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { cancel() }
    enqueue(
        object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(
                            ApiException.Http(response.code, null, "사진 업로드 실패"),
                        )
                    }
                }
            }
        },
    )
}
