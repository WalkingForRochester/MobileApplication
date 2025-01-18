package com.walkingforrochester.walkingforrochester.android.repository.internal

import android.content.Context
import android.net.Uri
import com.walkingforrochester.walkingforrochester.android.WFRDateFormatter
import com.walkingforrochester.walkingforrochester.android.di.IODispatcher
import com.walkingforrochester.walkingforrochester.android.md5
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.model.Leader
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardPeriod
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardType
import com.walkingforrochester.walkingforrochester.android.model.ProfileException
import com.walkingforrochester.walkingforrochester.android.network.RestApiService
import com.walkingforrochester.walkingforrochester.android.network.request.AccountIdRequest
import com.walkingforrochester.walkingforrochester.android.network.request.EmailAddressRequest
import com.walkingforrochester.walkingforrochester.android.network.request.LeaderboardRequest
import com.walkingforrochester.walkingforrochester.android.network.request.LoginRequest
import com.walkingforrochester.walkingforrochester.android.network.request.RegisterRequest
import com.walkingforrochester.walkingforrochester.android.network.request.UpdateProfileRequest
import com.walkingforrochester.walkingforrochester.android.network.response.toLeaderList
import com.walkingforrochester.walkingforrochester.android.repository.NetworkRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.FileNotFoundException
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val restApiService: RestApiService,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : NetworkRepository {

    override suspend fun fetchProfile(accountId: Long): AccountProfile {

        val response = restApiService.userProfile(AccountIdRequest(accountId = accountId))
        if (response.error.isNullOrBlank()) {
            return response.toAccountProfile()
        } else {
            throw ProfileException(response.error)
        }
    }

    override suspend fun fetchAccountId(email: String): Long {

        // Allow result to return regardless of error text. If it is an error, the
        // account id will be NO_ACCOUNT
        val result = restApiService.accountByEmail(EmailAddressRequest(email = email))
        return result.accountId ?: AccountProfile.NO_ACCOUNT
    }

    override suspend fun isEmailInUse(email: String): Boolean {
        return fetchAccountId(email) != AccountProfile.NO_ACCOUNT
    }

    override suspend fun updateProfile(profile: AccountProfile) {
        restApiService.updateProfile(
            UpdateProfileRequest(
                accountId = profile.accountId,
                email = profile.email,
                phone = profile.phoneNumber,
                nickname = profile.nickname,
                communityService = profile.communityService,
                imgUrl = profile.imageUrl,
                facebookId = profile.facebookId
            )
        )
    }

    override suspend fun performLogin(email: String, password: String): Long {
        val response = restApiService.login(LoginRequest(email, password))

        if (response.error.isNullOrBlank()) {
            return response.accountId ?: AccountProfile.NO_ACCOUNT
        } else {
            throw ProfileException(response.error)
        }
    }

    override suspend fun registerAccount(
        profile: AccountProfile,
        password: String
    ): Long {
        val response = restApiService.registerAccount(
            RegisterRequest(
                firstName = profile.firstName,
                lastName = profile.lastName,
                email = profile.email,
                phone = profile.phoneNumber,
                nickname = profile.nickname,
                dateOfBirth = LocalDate.now(),
                password = password,
                communityService = profile.communityService,
                facebookId = profile.facebookId
            )
        )

        if (response.error.isNullOrBlank()) {
            return response.accountId ?: AccountProfile.NO_ACCOUNT
        } else {
            throw ProfileException(response.error)
        }
    }

    override suspend fun forgotPassword(email: String): String {
        val result = restApiService.forgotPassword(EmailAddressRequest(email = email))
        return result.code
    }

    override suspend fun resetPassword(email: String, password: String) {
        restApiService.resetPassword(LoginRequest(email, password))
    }

    override suspend fun fetchLeaderboard(
        period: LeaderboardPeriod
    ): List<Leader> = withContext(ioDispatcher) {
        val startDate = when (period) {
            LeaderboardPeriod.Day -> LocalDate.now()
            LeaderboardPeriod.Week -> LocalDate.now().minusWeeks(1)
            LeaderboardPeriod.Month -> LocalDate.now().minusMonths(1)
            LeaderboardPeriod.Year -> LocalDate.now().minusYears(1)
        }
        val endDate = LocalDate.now()

        // Always fetching the collection, the view model will sort data
        val result = restApiService.leaderboard(
            LeaderboardRequest(
                orderBy = LeaderboardType.Collection.name.lowercase(),
                startDate = startDate,
                endDate = endDate
            )
        )

        result.toLeaderList()
    }

    override suspend fun uploadProfileImage(
        accountId: Long,
        imageUri: Uri,
        time: LocalDate
    ): String {
        val fileName = "IMG_PROFILE_${
            time.format(WFRDateFormatter.formatter)
        }_${md5(accountId.toString())}"

        return when (uploadImage(imageUri, fileName)) {
            true -> "https://walkingforrochester.com/images/profile/$fileName.jpg"
            else -> ""
        }
    }

    override suspend fun uploadWalkImage(
        accountId: Long,
        imageUri: Uri,
        time: LocalDate
    ): String {
        val fileName = "IMG_WALKING_PICKIMAGE_${
            time.format(WFRDateFormatter.formatter)
        }_${md5(accountId.toString())}"

        return when (uploadImage(imageUri, fileName)) {
            true -> fileName
            else -> ""
        }
    }

    override suspend fun deleteUser(accountId: Long) {
        restApiService.deleteUser(AccountIdRequest(accountId = accountId))
    }

    private suspend fun uploadImage(
        imageUri: Uri,
        fileName: String
    ): Boolean = withContext(ioDispatcher) {

        when {
            imageUri.path.isNullOrBlank() -> false
            else -> {
                val bytes: ByteArray? = try {
                    context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                        inputStream.readBytes()
                    }
                } catch (e: FileNotFoundException) {
                    Timber.w("Failed to read image: %s: %s", imageUri, e.message)
                    null
                }

                when {
                    bytes == null -> false
                    bytes.isEmpty() -> false
                    else -> {
                        val body = MultipartBody.Part.createFormData(
                            "file",
                            fileName,
                            bytes.toRequestBody("form-data".toMediaTypeOrNull())
                        )

                        restApiService.uploadImage(body).isSuccessful
                    }
                }
            }
        }
    }
}