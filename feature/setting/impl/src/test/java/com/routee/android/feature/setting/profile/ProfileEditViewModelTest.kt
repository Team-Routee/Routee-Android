package com.routee.android.feature.setting.profile

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import com.routee.android.core.data.member.exception.MemberException
import com.routee.android.core.data.member.model.MemberProfile
import com.routee.android.core.domain.member.ValidateNicknameUseCase
import com.routee.android.feature.setting.FakeProfileRepository
import com.routee.android.feature.setting.MainDispatcherRule
import com.routee.android.feature.setting.profile.model.ProfileEditSideEffect
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val PROFILE_IMAGE_URL = "https://image/profile.jpg"
private const val CROPPED_IMAGE_URI = "content://cropped.jpg"

class ProfileEditViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProfileRepository()

    private fun createViewModel() = ProfileEditViewModel(repository, ValidateNicknameUseCase())

    @Test
    fun `진입하면 프로필을 불러와 닉네임과 사진을 채운다`() {
        repository.profileResult = Result.success(MemberProfile("관악산", PROFILE_IMAGE_URL))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("관악산", viewModel.nicknameState.text.toString())
        assertEquals(PROFILE_IMAGE_URL, state.profileImageUrl)
        assertFalse(state.isSaveEnabled)
    }

    @Test
    fun `프로필 조회에 실패하면 에러 화면을 띄우고 다시 시도하면 다시 불러온다`() {
        repository.profileResult = Result.failure(IOException())
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.isLoadFailed)

        repository.profileResult = Result.success(MemberProfile("관악산", null))
        viewModel.onRetryClick()

        assertFalse(viewModel.uiState.value.isLoadFailed)
        assertEquals(2, repository.profileCount)
    }

    @Test
    fun `사진이 없으면 카메라 버튼에서 갤러리를 바로 연다`() = runTest {
        val viewModel = createViewModel()

        viewModel.onProfileImageClick()

        assertFalse(viewModel.uiState.value.isPhotoDialogVisible)
        assertEquals(ProfileEditSideEffect.OpenGallery, viewModel.sideEffect.first())
    }

    @Test
    fun `사진이 있으면 카메라 버튼에서 사진 설정 모달을 띄운다`() {
        repository.profileResult = Result.success(MemberProfile("관악산", PROFILE_IMAGE_URL))
        val viewModel = createViewModel()

        viewModel.onProfileImageClick()

        assertTrue(viewModel.uiState.value.isPhotoDialogVisible)
    }

    @Test
    fun `자르기에서 뒤로 가면 갤러리를 다시 연다`() = runTest {
        val viewModel = createViewModel()

        viewModel.onProfileImageCropCancel()

        assertEquals(ProfileEditSideEffect.OpenGallery, viewModel.sideEffect.first())
    }

    @Test
    fun `닉네임만 바꾸면 닉네임만 저장한다`() = runTest {
        val viewModel = createViewModel()

        viewModel.nicknameState.setTextAndPlaceCursorAtEnd("루티")
        viewModel.onSaveClick()

        assertEquals(listOf("루티"), repository.updatedNicknames)
        assertTrue(repository.uploadedImageUris.isEmpty())
        assertEquals(0, repository.resetCount)
        assertFalse(viewModel.uiState.value.isSaveEnabled)
        assertEquals(ProfileEditSideEffect.ShowSavedToast, viewModel.sideEffect.first())
    }

    @Test
    fun `자른 사진을 저장하면 업로드하고 받은 URL로 바꾼다`() = runTest {
        val viewModel = createViewModel()

        viewModel.onProfileImageCropComplete(CROPPED_IMAGE_URI)
        assertTrue(viewModel.uiState.value.isSaveEnabled)
        viewModel.onSaveClick()

        assertTrue(repository.updatedNicknames.isEmpty())
        assertEquals(listOf(CROPPED_IMAGE_URI), repository.uploadedImageUris)
        assertEquals("https://image/uploaded.jpg", viewModel.uiState.value.profileImageUrl)
        assertFalse(viewModel.uiState.value.isSaveEnabled)
        assertEquals(ProfileEditSideEffect.ShowSavedToast, viewModel.sideEffect.first())
    }

    @Test
    fun `기본 이미지를 적용하고 저장하면 기본 이미지 API를 부른다`() {
        repository.profileResult = Result.success(MemberProfile("관악산", PROFILE_IMAGE_URL))
        val viewModel = createViewModel()

        viewModel.onDefaultImageClick()
        viewModel.onSaveClick()

        assertEquals(1, repository.resetCount)
        assertNull(viewModel.uiState.value.profileImageUrl)
        assertFalse(viewModel.uiState.value.isSaveEnabled)
    }

    @Test
    fun `닉네임 저장에 실패하면 사진은 올리지 않고 네트워크 토스트를 띄운다`() = runTest {
        repository.nicknameResult = Result.failure(MemberException.NetworkUnavailable(null))
        val viewModel = createViewModel()

        viewModel.nicknameState.setTextAndPlaceCursorAtEnd("루티")
        viewModel.onProfileImageCropComplete(CROPPED_IMAGE_URI)
        viewModel.onSaveClick()

        assertTrue(repository.uploadedImageUris.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isSaveEnabled)
        assertEquals(ProfileEditSideEffect.ShowNetworkErrorToast, viewModel.sideEffect.first())
    }

    @Test
    fun `사진 저장만 실패하면 저장된 닉네임은 유지하고 사진만 다시 저장할 수 있다`() = runTest {
        repository.profileImageResult = Result.failure(IllegalStateException())
        val viewModel = createViewModel()

        viewModel.nicknameState.setTextAndPlaceCursorAtEnd("루티")
        viewModel.onProfileImageCropComplete(CROPPED_IMAGE_URI)
        viewModel.onSaveClick()

        val state = viewModel.uiState.value
        assertEquals("루티", state.savedNickname)
        assertFalse(state.isNicknameChanged)
        assertTrue(state.isProfileImageChanged)
        assertEquals(ProfileEditSideEffect.ShowUnknownErrorToast, viewModel.sideEffect.first())
    }

    @Test
    fun `유효하지 않은 닉네임이면 저장하지 않는다`() {
        val viewModel = createViewModel()

        viewModel.nicknameState.setTextAndPlaceCursorAtEnd("관악산^&^")
        viewModel.onSaveClick()

        assertTrue(repository.updatedNicknames.isEmpty())
    }
}
