package com.routee.android.feature.onboarding.login

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class GoogleLoginManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    suspend fun clearCredential(): Result<Unit> = try {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        Result.success(Unit)
    } catch (e: ClearCredentialException) {
        Result.failure(e)
    }
}
