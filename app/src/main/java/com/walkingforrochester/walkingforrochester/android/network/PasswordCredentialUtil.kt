package com.walkingforrochester.walkingforrochester.android.network

import android.content.Context
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import timber.log.Timber

object PasswordCredentialUtil {

    private fun buildPasswordCredentialRequest(): GetCredentialRequest {

        return GetCredentialRequest.Builder()
            .addCredentialOption(GetPasswordOption())
            .build()
    }

    suspend fun performPasswordSignIn(
        activityContext: Context,
        performLogin: (String, String) -> Unit
    ) {
        val credentialManager = CredentialManager.create(activityContext.applicationContext)
        try {
            val result = credentialManager.getCredential(
                request = buildPasswordCredentialRequest(),
                context = activityContext,
            )
            handlePasswordResponse(result, performLogin)
        } catch (e: GetCredentialException) {
            Timber.w("Failed to get credential: %s", e.message)
        }
    }

    private fun handlePasswordResponse(
        result: GetCredentialResponse,
        performLogin: (String, String) -> Unit
    ) {

        // Handle the successfully returned credential.
        when (val credential = result.credential) {
            is PasswordCredential -> {
                val email = credential.id
                val password = credential.password
                performLogin(email, password)
            }

            else -> {
                // Catch any unrecognized credential type here.
                Timber.w("Unexpected credential type: %s", credential.type)
            }
        }
    }

    suspend fun savePasswordCredential(
        activityContext: Context,
        email: String,
        password: String
    ) {
        val credentialManager = CredentialManager.create(activityContext.applicationContext)
        try {
            credentialManager.createCredential(
                context = activityContext,
                request = CreatePasswordRequest(
                    id = email,
                    password = password
                )
            )
        } catch (e: CreateCredentialException) {
            Timber.w("Failed to create credential: %s", e.message)
        }
    }
}