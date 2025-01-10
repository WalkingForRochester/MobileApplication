package com.walkingforrochester.walkingforrochester.android.repository.internal

import android.content.ContentResolver
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.squareup.moshi.Moshi
import com.walkingforrochester.walkingforrochester.android.LocalDateAdapter
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.WFRDateFormatter
import com.walkingforrochester.walkingforrochester.android.md5
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.model.ProfileException
import com.walkingforrochester.walkingforrochester.android.network.RestApiService
import com.walkingforrochester.walkingforrochester.android.network.buildHttpClient
import com.walkingforrochester.walkingforrochester.android.network.installServerClientCertificate
import com.walkingforrochester.walkingforrochester.android.repository.NetworkRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.internal.commonToUtf8String
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.HttpURLConnection
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class NetworkRepositoryImplTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockWebServer: MockWebServer

    private lateinit var networkRepository: NetworkRepository

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockWebServer = MockWebServer()
        // This also sets up the http client to talk to this server...
        val mockClient = mockWebServer.buildHttpClient()
        mockWebServer.installServerClientCertificate()
        mockWebServer.start()

        val moshi = Moshi.Builder()
            .add(LocalDateAdapter())
            .build()

        // Tells Retrofit to use dummy url to mock server when building rest services
        val mockApiService = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(mockWebServer.url("/test/").toString())
            .client(mockClient)
            .build()
            .create(RestApiService::class.java)

        networkRepository = NetworkRepositoryImpl(
            context = context,
            restApiService = mockApiService,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        Dispatchers.resetMain()
    }

    @Test
    fun testFetchProfile() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(buildAccountResponse())
        )

        val profile = networkRepository.fetchProfile(ACCOUNT_ID)
        validateProfile(profile)

        // Test empty body sample (shouldn't really happen but tests default logic)
        mockWebServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody("{}")
        )
        val profile2 = networkRepository.fetchProfile(ACCOUNT_ID)
        assertEquals(AccountProfile.NO_ACCOUNT, profile2.accountId)
        assertEquals("", profile2.email)
        assertEquals(0.0, profile2.distanceToday)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(buildErrorAccountResponse())
        )

        // Test api error response
        testErrorMessage {
            networkRepository.fetchProfile(ACCOUNT_ID)
        }

        // Test http error
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND))

        testHttpError {
            networkRepository.fetchProfile(ACCOUNT_ID)
        }
    }

    @Test
    fun testFetchProfileByEmail() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(buildAccountResponse())
        )

        val accountId = networkRepository.fetchAccountId(EMAIL)
        assertEquals(ACCOUNT_ID, accountId)

        // Test empty body sample (shouldn't really happen but tests default logic)
        mockWebServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody("{}")
        )
        val accountId2 = networkRepository.fetchAccountId(EMAIL)
        assertEquals(AccountProfile.NO_ACCOUNT, accountId2)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(buildErrorAccountResponse())
        )

        // Test api error response. For email fetches due to account recovery, this
        // will return a profile instead of an error. However, the profile isn't valid
        val accountId3 = networkRepository.fetchAccountId(EMAIL)
        assertEquals(AccountProfile.NO_ACCOUNT, accountId3)

        // Test http error
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND))

        testHttpError {
            networkRepository.fetchAccountId(EMAIL)
        }
    }

    @Test
    fun testIsEmailInUse() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(buildAccountResponse())
        )

        assertEquals(true, networkRepository.isEmailInUse(EMAIL))

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(buildErrorAccountResponse())
        )
        assertEquals(false, networkRepository.isEmailInUse(EMAIL))

        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND))
        // Test api error response
        testHttpError {
            networkRepository.isEmailInUse(EMAIL)
        }
    }

    private fun validateProfile(profile: AccountProfile) {
        assertEquals(ACCOUNT_ID, profile.accountId)
        assertEquals(EMAIL, profile.email)
        assertEquals(FIRST_NAME, profile.firstName)
        assertEquals(LAST_NAME, profile.lastName)
        assertEquals(PHONE, profile.phoneNumber)
        assertEquals(IMG_URL, profile.imageUrl)
        assertEquals(NICKNAME, profile.nickname)
        assertEquals(true, profile.communityService)
        assertEquals(DISTANCE, profile.distanceToday)
        assertEquals(TOTAL_DISTANCE, profile.totalDistance)
        assertEquals(DURATION, profile.durationToday)
        assertEquals(TOTAL_DURATION, profile.totalDuration)
        assertEquals(FACEBOOK_ID, profile.facebookId)
    }

    @Test
    fun testUpdateProfile() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK))
        networkRepository.updateProfile(
            AccountProfile.DEFAULT_PROFILE.copy(
                accountId = 1,
                email = EMAIL,
                phoneNumber = PHONE,
                communityService = true,
                nickname = NICKNAME,
                imageUrl = IMG_URL,
                facebookId = FACEBOOK_ID
            )
        )

        assertEquals(1, mockWebServer.requestCount)
        val request = mockWebServer.takeRequest()
        val json = JSONObject(request.body.readUtf8())
        assertEquals(ACCOUNT_ID, json.getLong("accountId"))
        assertEquals(EMAIL, json.getString("email"))
        assertEquals(NICKNAME, json.getString("nickname"))
        assertEquals(true, json.getBoolean("communityService"))
        assertEquals(PHONE, json.getString("phone"))
        assertEquals(IMG_URL, json.getString("imgUrl"))

        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND))

        testHttpError {
            networkRepository.updateProfile(AccountProfile.DEFAULT_PROFILE)
        }
    }

    @Test
    fun testLogin() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(buildAccountResponse())
        )

        val accountId = networkRepository.performLogin(email = EMAIL, password = PASSWORD)
        assertEquals(accountId, ACCOUNT_ID)

        assertEquals(1, mockWebServer.requestCount)
        val request = mockWebServer.takeRequest()
        val json = JSONObject(request.body.readUtf8())
        assertEquals(EMAIL, json.getString("email"))
        assertEquals(PASSWORD, json.getString("password"))

        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(buildErrorAccountResponse())
        )
        testErrorMessage {
            networkRepository.performLogin(email = EMAIL, password = PASSWORD)
        }

        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND))

        testHttpError {
            networkRepository.performLogin(email = EMAIL, password = PASSWORD)
        }
    }

    @Test
    fun testUploadProfileImage() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK))
        val imageUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.packageName)
            .path(R.drawable.wfr_logo_small.toString())
            .build()

        val time = LocalDate.now()
        val result = networkRepository.uploadProfileImage(
            accountId = 12345,
            imageUri = imageUri,
            time = time
        )

        val expectedFile = "IMG_PROFILE_${
            time.format(WFRDateFormatter.formatter)
        }_${md5(12345.toString())}"

        assertEquals("https://walkingforrochester.com/images/profile/${expectedFile}.jpg", result)

        validateImageResponse(imageUri)

        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR))
        val result2 = networkRepository.uploadProfileImage(
            accountId = 12334,
            imageUri = Uri.EMPTY,
            time = time
        )

        assertEquals("", result2)
    }

    @Test
    fun testUploadWalkImage() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK))
        val imageUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.packageName)
            .path(R.drawable.wfr_logo_small.toString())
            .build()

        val time = LocalDate.now()
        val result = networkRepository.uploadWalkImage(
            accountId = 12345,
            imageUri = imageUri,
            time = time
        )

        val expectedFile = "IMG_WALKING_PICKIMAGE_${
            time.format(WFRDateFormatter.formatter)
        }_${md5(12345.toString())}"

        assertEquals(expectedFile, result)

        validateImageResponse(imageUri)

        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR))
        val result2 = networkRepository.uploadWalkImage(
            accountId = 12334,
            imageUri = Uri.EMPTY,
            time = time
        )

        assertEquals("", result2)
    }

    @Test
    fun testDeleteAccount() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK))
        networkRepository.deleteUser(ACCOUNT_ID)

        assertEquals(1, mockWebServer.requestCount)
        val request = mockWebServer.takeRequest()
        val json = JSONObject(request.body.readUtf8())
        assertEquals(ACCOUNT_ID, json.getLong("accountId"))

        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND))

        testHttpError {
            networkRepository.deleteUser(ACCOUNT_ID)
        }
    }

    private fun validateImageResponse(imageUri: Uri) {
        assertEquals(1, mockWebServer.requestCount)
        val request = mockWebServer.takeRequest()

        assertEquals("POST", request.method)
        val contentType = request.getHeader("content-type")
        assertEquals(
            "content-type mismatch",
            true,
            contentType?.startsWith("multipart/form-data; boundary=")
        )

        // Verify image in body...
        val bytes = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            inputStream.readBytes()
        } ?: byteArrayOf()

        val utfBytes = bytes.commonToUtf8String()

        val body = request.body.readUtf8()

        assertEquals("Image not in body", true, body.contains(utfBytes))
    }

    private suspend fun testErrorMessage(call: suspend () -> Unit) {
        var haveError = false
        try {
            call()
        } catch (t: Throwable) {
            assertEquals(ERROR, t.message)
            haveError = t is ProfileException
        }
        assertEquals(true, haveError)
    }

    private suspend fun testHttpError(call: suspend () -> Unit) {
        var haveError = false
        try {
            call()
        } catch (t: Throwable) {
            haveError = t is HttpException
        }
        assertEquals(true, haveError)
    }

    private fun buildAccountResponse(): String {
        val json = JSONObject().apply {
            put("accountId", ACCOUNT_ID)
            put("email", EMAIL)
            put("firstName", FIRST_NAME)
            put("lastName", LAST_NAME)
            put("phoneNumber", PHONE)
            put("imgUrl", IMG_URL)
            put("nickname", NICKNAME)
            put("communityService", true)
            put("distance", DISTANCE)
            put("totalDistance", TOTAL_DISTANCE)
            put("duration", DURATION)
            put("totalDuration", TOTAL_DURATION)
            put("facebookId", FACEBOOK_ID)
        }
        return json.toString()
    }

    private fun buildErrorAccountResponse(): String {
        val json = JSONObject().apply {
            put("error", ERROR)
        }
        return json.toString()
    }

    companion object {
        const val ACCOUNT_ID = 1L
        const val EMAIL = "test@email.com"
        const val FIRST_NAME = "test"
        const val LAST_NAME = "account"
        const val PHONE = "5551234567"
        const val NICKNAME = "tester"
        const val IMG_URL = "http://example.com/image/test.jpg"
        const val DISTANCE = 1.1
        const val TOTAL_DISTANCE = 5.3
        const val DURATION = 1200L
        const val TOTAL_DURATION = 10000L
        const val FACEBOOK_ID = "fb1"
        const val ERROR = "error message"
        const val PASSWORD = "password"
    }
}