package com.routee.android.feature.workout.recording

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// 기록편집 FileProvider 와 매니페스트에서 겹치지 않게 따로 둠
internal class WorkoutPhotoFileProvider : FileProvider() {
    companion object {
        fun uriFor(
            context: Context,
            file: File,
        ): Uri = getUriForFile(context, "${context.packageName}.workout.fileprovider", file)
    }
}
