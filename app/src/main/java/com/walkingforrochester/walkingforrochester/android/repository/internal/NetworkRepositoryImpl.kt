package com.walkingforrochester.walkingforrochester.android.repository.internal

import android.content.Context
import android.net.Uri
import com.walkingforrochester.walkingforrochester.android.WFRDateFormatter
import com.walkingforrochester.walkingforrochester.android.di.IODispatcher
import com.walkingforrochester.walkingforrochester.android.md5
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.network.RestApiService
import com.walkingforrochester.walkingforrochester.android.network.request.AccountIdRequest
import com.walkingforrochester.walkingforrochester.android.network.request.EmailAddressRequest
import com.walkingforrochester.walkingforrochester.android.network.request.UpdateProfileRequest
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
            throw RuntimeException(response.error)
        }
    }

    override suspend fun fetchProfile(email: String): AccountProfile {

        val response = restApiService.accountByEmail(EmailAddressRequest(email = email))
        if (response.error.isNullOrBlank()) {
            return response.toAccountProfile()
        } else {
            throw RuntimeException(response.error)
        }
    }

    override suspend fun isEmailInUse(email: String): Boolean {
        val response = restApiService.accountByEmail(EmailAddressRequest(email = email))
        Timber.d("Response: %s", response)
        return response.accountId != null
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