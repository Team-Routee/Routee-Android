package com.routee.android.feature.onboarding.login

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

internal suspend fun Activity.requestGoogleIdToken(): Result<String> {
    return try {
        val credential = CredentialManager.create(this)
            .getCredential(this, googleIdRequest())
            .credential
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return Result.failure(GoogleLoginException.Failed())
        }
        Result.success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
    } catch (e: GetCredentialCancellationException) {
        Result.failure(
            if (isNetworkAvailable()) GoogleLoginException.Cancelled(e) else GoogleLoginException.NetworkUnavailable(e),
        )
    } catch (e: NoCredentialException) {
        Result.failure(
            if (isNetworkAvailable()) GoogleLoginException.NoAccount(e) else GoogleLoginException.NetworkUnavailable(e),
        )
    } catch (e: GetCredentialException) {
        Result.failure(
            if (isNetworkAvailable()) GoogleLoginException.Failed(e) else GoogleLoginException.NetworkUnavailable(e),
        )
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

private fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(ConnectivityManager::class.java) ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
