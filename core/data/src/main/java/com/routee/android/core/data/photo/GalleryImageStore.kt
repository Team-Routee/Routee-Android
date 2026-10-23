package com.routee.android.core.data.photo

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.routee.android.core.common.util.suspendRunCatching
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 원본 이미지의 주소와 촬영 시각을 받아 기기 갤러리에 복사하는 저장소 */
class GalleryImageStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** 이미지 주소와 촬영 시각으로 갤러리 저장, 실패 원인은 Result로 반환 */
    suspend fun save(imageUrl: String, capturedAtEpochMillis: Long): Result<Unit> = suspendRunCatching {
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(imageUrl)
            val connection = if (uri.scheme == "http" || uri.scheme == "https") {
                URL(imageUrl).openConnection().apply {
                    connectTimeout = 15_000
                    readTimeout = 15_000
                }
            } else {
                null
            }
            try {
                val source = when (uri.scheme) {
                    "http", "https" -> checkNotNull(connection).getInputStream()
                    "content", "file", "android.resource" -> checkNotNull(context.contentResolver.openInputStream(uri))
                    null -> File(imageUrl).inputStream()
                    else -> error("Unsupported image address")
                }
                source.buffered().use { input ->
                    val provided = if (connection != null) {
                        connection.contentType
                    } else if (uri.scheme == "content" || uri.scheme == "android.resource") {
                        context.contentResolver.getType(uri)
                    } else {
                        null
                    }
                    val extension = uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase(Locale.ROOT)
                    val mimeType = imageMimeType(
                        input,
                        provided,
                        extension?.let(MimeTypeMap.getSingleton()::getMimeTypeFromExtension),
                    )
                    val suffix = checkNotNull(MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType))
                    val name = "Routee_${capturedAtEpochMillis}_${System.currentTimeMillis()}.$suffix"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        saveToMediaStore(context, input, name, mimeType, capturedAtEpochMillis)
                    } else {
                        saveToLegacyGallery(context, input, name, mimeType)
                    }
                }
            } finally {
                (connection as? HttpURLConnection)?.disconnect()
            }
        }
    }
}

private fun saveToMediaStore(context: Context, source: InputStream, name: String, mimeType: String, capturedAt: Long) {
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        put(MediaStore.Images.Media.DATE_TAKEN, capturedAt)
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Routee")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        ?: error("Unable to create gallery image")
    try {
        resolver.openOutputStream(uri, "w")?.use { output -> check(source.copyTo(output) > 0) }
            ?: error("Unable to open gallery image")
        val published = resolver.update(
            uri,
            ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            },
            null,
            null,
        )
        check(published == 1)
    } catch (error: Exception) {
        runCatching { resolver.delete(uri, null, null) }
        throw error
    }
}

@Suppress("DEPRECATION")
private fun saveToLegacyGallery(context: Context, source: InputStream, name: String, mimeType: String) {
    val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Routee")
    check(directory.isDirectory || directory.mkdirs())
    val file = File(directory, name)
    try {
        file.outputStream().use { output -> check(source.copyTo(output) > 0) }
        MediaScannerConnection.scanFile(context, arrayOf(file.path), arrayOf(mimeType), null)
    } catch (error: Exception) {
        file.delete()
        throw error
    }
}
