package com.walkingforrochester.walkingforrochester.android.network

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.walkingforrochester.walkingforrochester.android.R
import timber.log.Timber

class GoogleApiContract : ActivityResultContract<Int, Task<GoogleSignInAccount>?>() {
    override fun createIntent(context: Context, input: Int): Intent {
        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(input))
                .requestEmail()
                .build()
        val intent = GoogleSignIn.getClient(context, gso)
        return intent.signInIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Task<GoogleSignInAccount>? {
        return when (resultCode) {
            Activity.RESULT_OK -> {
                GoogleSignIn.getSignedInAccountFromIntent(intent)
            }

            else -> null
        }
    }

}

fun GoogleLoginCallback(
    context: Context,
    task: Task<GoogleSignInAccount>?,
    parseResult: (GoogleSignInAccount) -> Unit
) {
    try {
        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.server_client_id))
                .requestEmail()
                .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        val gsa = task?.getResult(ApiException::class.java)
        if (gsa != null) {
            googleSignInClient.signOut()
            googleSignInClient.revokeAccess()
            parseResult(gsa)
        }
    } catch (e: ApiException) {
        Timber.e(e)
    }
}