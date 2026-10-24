package com.routee.android.core.data.image.repository

import android.content.Context
import coil3.SingletonImageLoader
import com.routee.android.core.common.util.suspendRunCatching
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ImageCacheRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ImageCacheRepository {
    override suspend fun clear(): Result<Unit> = suspendRunCatching {
        withContext(Dispatchers.IO) {
            val imageLoader = SingletonImageLoader.get(context)
            imageLoader.memoryCache?.clear()
            imageLoader.diskCache?.clear()
        }
    }
}
