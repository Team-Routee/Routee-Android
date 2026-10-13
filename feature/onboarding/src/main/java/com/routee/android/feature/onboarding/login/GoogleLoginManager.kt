package com.routee.android.feature.onboarding.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.routee.android.feature.onboarding.BuildConfig
import javax.inject.Inject

internal class GoogleLoginManager @Inject constructor() {

    suspend fun requestIdToken(activityContext: Context): Result<String> {
        return try {
            val credential = CredentialManager.create(activityContext)
                .getCredential(activityContext, googleIdRequest())
                .credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return Result.failure(GoogleLoginException.Failed())
            }
            Result.success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } catch (e: GetCredentialCancellationException) {
            Result.failure(GoogleLoginException.Cancelled(e))
        } catch (e: NoCredentialException) {
            Result.failure(GoogleLoginException.NoAccount(e))
        } catch (e: GetCredentialException) {
            Result.failure(GoogleLoginException.Failed(e))
        } catch (e: GoogleIdTokenParsingException) {
            Result.failure(GoogleLoginException.Failed(e))
        } catch (e: IllegalArgumentException) {
            Result.failure(GoogleLoginException.Failed(e))
        }
    }

    private fun googleIdRequest(): GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(
            GetSignInWithGoogleOption.Builder(serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID).build(),
        )
        .build()
}
