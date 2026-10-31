package com.routee.android.core.ui.photo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.routee.android.core.designsystem.component.message.RouteeMessage
import com.routee.android.core.designsystem.component.message.rememberRouteeMessageState
import com.routee.android.core.ui.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 저장 완료 페이지·결과 문구와 사진 주소·촬영 시각으로 요청하는 갤러리 저장 이벤트 */
@Immutable
class GalleryPhotoSaveState internal constructor(
    val savedPhotoIndex: Int,
    val saveMessage: String?,
    val onSavePhoto: (index: Int, imageUrl: String, capturedAtEpochMillis: Long) -> Unit,
)

/** 권한 요청·저장 결과 안내·2초 완료 표시를 관리하는 갤러리 저장 상태 */
@Composable
fun rememberGalleryPhotoSaveState(
    savePhoto: suspend (imageUrl: String, capturedAtEpochMillis: Long) -> Result<Unit>,
): GalleryPhotoSaveState {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val messageState = rememberRouteeMessageState()
    val savedText = stringResource(R.string.ui_photo_saved)
    val deniedText = stringResource(R.string.ui_photo_access_denied)
    val failedText = stringResource(R.string.ui_photo_save_failed)
    var savedIndex by remember { mutableIntStateOf(-1) }
    var isSaving by remember { mutableStateOf(false) }
    val currentSavePhoto by rememberUpdatedState(savePhoto)
    var pendingSave by rememberSaveable(stateSaver = GallerySaveRequestSaver) {
        mutableStateOf<GallerySaveRequest?>(null)
    }

    LaunchedEffect(savedIndex) {
        if (savedIndex >= 0) {
            delay(2_000)
            savedIndex = -1
        }
    }
    fun save(request: GallerySaveRequest) {
        if (isSaving || savedIndex == request.index) return
        isSaving = true
        scope.launch {
            try {
                currentSavePhoto(request.imageUrl, request.capturedAt)
                    .onSuccess {
                        savedIndex = request.index
                        messageState.showToast(savedText)
                    }.onFailure { error ->
                        messageState.showToast(if (error is SecurityException) deniedText else failedText)
                    }
            } finally {
                isSaving = false
            }
        }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val request = pendingSave
        pendingSave = null
        if (granted && request != null) save(request)
        if (!granted) messageState.showToast(deniedText)
    }
    val saveMessage = (messageState.currentMessage as? RouteeMessage.Toast)?.text
    return GalleryPhotoSaveState(savedIndex, saveMessage) { index, url, capturedAt ->
        if (!isSaving && pendingSave == null && savedIndex != index) {
            val request = GallerySaveRequest(index, url, capturedAt)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                pendingSave = request
                launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                save(request)
            }
        }
    }
}

private data class GallerySaveRequest(val index: Int, val imageUrl: String, val capturedAt: Long)

private val GallerySaveRequestSaver = listSaver<GallerySaveRequest?, Any>(
    save = { request ->
        if (request == null) emptyList() else listOf(request.index, request.imageUrl, request.capturedAt)
    },
    restore = { values ->
        if (values.isEmpty()) null else GallerySaveRequest(values[0] as Int, values[1] as String, values[2] as Long)
    },
)
