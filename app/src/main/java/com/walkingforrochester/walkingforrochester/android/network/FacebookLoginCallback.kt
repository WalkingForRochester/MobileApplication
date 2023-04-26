package com.walkingforrochester.walkingforrochester.android.network

import android.os.Bundle
import com.facebook.AccessToken
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import org.json.JSONObject
import timber.log.Timber


class FacebookLoginCallback(private val parseResult: (JSONObject) -> Unit) :
    FacebookCallback<LoginResult> {

    override fun onSuccess(result: LoginResult) {
        val request = GraphRequest.newMeRequest(
            result.accessToken
        ) { obj, _ ->
            if (obj != null) {
                try {
                    disconnectFromFacebook()

                    parseResult(obj)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }

        val parameters = Bundle()
        parameters.putString(
            "fields", "first_name, last_name, email"
        )
        request.parameters = parameters
        request.executeAsync()
    }

    override fun onCancel() {
        println("onCancel")
    }

    override fun onError(error: FacebookException) {
        println("onError $error")
    }

    private fun disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return  // already logged out
        }
        GraphRequest(AccessToken.getCurrentAccessToken(),
            "/me/permissions/",
            null,
            HttpMethod.DELETE,
            { LoginManager.getInstance().logOut() }).executeAsync()
    }

}