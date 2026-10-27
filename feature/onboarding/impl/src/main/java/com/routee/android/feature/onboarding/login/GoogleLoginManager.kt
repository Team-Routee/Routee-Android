package com.routee.android.feature.onboarding.login

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import com.routee.android.core.common.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

internal class GoogleLoginManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val externalScope: CoroutineScope,
) {

    private suspend fun clearCredential(): Result<Unit> = try {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        Result.success(Unit)
    } catch (e: ClearCredentialException) {
        Result.failure(e)
    }

    fun clearCredentialInBackground() {
        externalScope.launch { clearCredential().onFailure { e -> Timber.w(e) } }
    }
}
