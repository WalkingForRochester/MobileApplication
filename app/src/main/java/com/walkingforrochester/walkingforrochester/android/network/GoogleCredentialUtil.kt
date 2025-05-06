package com.walkingforrochester.walkingforrochester.android.network

import android.content.Context
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.walkingforrochester.walkingforrochester.android.BuildConfig
import timber.log.Timber
import java.security.SecureRandom


object GoogleCredentialUtil {

    private var nonce = ""

    private fun generateNonce(): String {

        val seedBytes = SecureRandom().generateSeed(54)

        val nonceBytes = ByteArray(40) //you can change the length
        val sr = SecureRandom(seedBytes)
        sr.nextBytes(nonceBytes)  //randomise the bytes
        return Base64.encodeToString(nonceBytes, Base64.URL_SAFE)
    }

    private fun buildGoogleCredentialRequest(): GetCredentialRequest {

        nonce = generateNonce()

        val googleIdOption = GetSignInWithGoogleOption.Builder(BuildConfig.googleServerClientId)
            .setNonce(nonce)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    suspend fun performSignIn(
        activityContext: Context,
        processCredential: (GoogleIdTokenCredential) -> Unit
    ) {
        // Avoid leaks by using application context here
        val credentialManager = CredentialManager.create(activityContext.applicationContext)
        try {
            val result = credentialManager.getCredential(
                request = buildGoogleCredentialRequest(),
                context = activityContext,
            )
            handleGoogleSignInResponse(result, processCredential)
        } catch (e: GetCredentialException) {
            Timber.e("Failed to get credential: %s", e.message)
        }
    }

    suspend fun performSignOut(context: Context) {
        val credentialManager = CredentialManager.create(context.applicationContext)

        try {
            val request = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(request)
        } catch (e: ClearCredentialException) {
            Timber.e("Failed to clear credential: %s", e.message)
        }
    }

    private fun handleGoogleSignInResponse(
        result: GetCredentialResponse,
        processCredential: (GoogleIdTokenCredential) -> Unit
    ) {
        // Handle the successfully returned credential.
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        processCredential(googleIdTokenCredential)
                    } catch (e: GoogleIdTokenParsingException) {
                        Timber.e(e, "Received an invalid google id token response")
                    }
                } else {
                    // Catch any unrecognized credential type here.
                    Timber.e("Unexpected type of credential: %s", credential.type)
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Timber.e("Unexpected type of credential: %s", credential.javaClass.name)
            }
        }
    }
}