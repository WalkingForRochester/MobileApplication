package com.walkingforrochester.walkingforrochester.android.network

import com.walkingforrochester.walkingforrochester.android.network.request.AccountIdRequest
import com.walkingforrochester.walkingforrochester.android.network.request.EmailAddressRequest
import com.walkingforrochester.walkingforrochester.android.network.request.LeaderboardRequest
import com.walkingforrochester.walkingforrochester.android.network.request.LogAWalkRequest
import com.walkingforrochester.walkingforrochester.android.network.request.LoginRequest
import com.walkingforrochester.walkingforrochester.android.network.request.RegisterRequest
import com.walkingforrochester.walkingforrochester.android.network.request.UpdateProfileRequest
import com.walkingforrochester.walkingforrochester.android.network.response.AccountResponse
import com.walkingforrochester.walkingforrochester.android.network.response.CodeResponse
import com.walkingforrochester.walkingforrochester.android.network.response.LeaderResponse
import com.walkingforrochester.walkingforrochester.android.network.response.LoginResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RestApiService {

    @POST("login.php")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("accountByEmail.php")
    suspend fun accountByEmail(@Body emailAddressRequest: EmailAddressRequest): LoginResponse

    @POST("registerAccount.php")
    suspend fun registerAccount(@Body registerRequest: RegisterRequest): LoginResponse

    @POST("forgotPassword.php")
    suspend fun forgotPassword(@Body emailAddressRequest: EmailAddressRequest): CodeResponse

    @POST("resetPassword.php")
    suspend fun resetPassword(@Body resetPasswordRequest: LoginRequest)

    @POST("userProfile.php")
    suspend fun userProfile(@Body accountIdRequest: AccountIdRequest): AccountResponse

    @POST("updateProfile.php")
    suspend fun updateProfile(@Body updateProfileRequest: UpdateProfileRequest)

    @POST("deleteUser.php")
    suspend fun deleteUser(@Body accountIdRequest: AccountIdRequest)

    @Multipart
    @POST("uploadImage.php")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<Void>

    @POST("leaderboard.php")
    suspend fun leaderboard(@Body leaderboardRequest: LeaderboardRequest): List<LeaderResponse>

    @POST("logAWalk.php")
    suspend fun logAWalk(@Body logAWalkRequest: LogAWalkRequest)

    companion object {
        const val BASE_URL = "https://walkingforrochester.com/php/v2/"
    }
}