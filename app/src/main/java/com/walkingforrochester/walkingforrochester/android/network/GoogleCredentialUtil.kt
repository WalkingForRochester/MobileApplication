package com.walkingforrochester.walkingforrochester.android.network

import android.content.Context
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
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

        val googleIdOption = GetSignInWithGoogleOption.Builder("458901770892-os2jgpa48lt3ibmc7ud1pq56saj99e9i.apps.googleusercontent.com")//BuildConfig.googleServerClientId)
            .setNonce(nonce)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    suspend fun performSignIn(
        context: Context,
        processCredential: (GoogleIdTokenCredential) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)
        try {
            val result = credentialManager.getCredential(
                request = buildGoogleCredentialRequest(),
                context = context,
            )
            handleGoogleSignInResponse(result, processCredential)

            // Don't remember credential going forward so next login is normal
            val request = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(request)
        } catch (e: GetCredentialException) {
            Timber.e("Failed to get credential: %s", e.message)
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
                        Timber.e("Received an invalid google id token response", e)
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