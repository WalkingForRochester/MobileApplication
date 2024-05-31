package com.walkingforrochester.walkingforrochester.android.di

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.walkingforrochester.walkingforrochester.android.BuildConfig
import com.walkingforrochester.walkingforrochester.android.LocalDateAdapter
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.network.BASE_URL
import com.walkingforrochester.walkingforrochester.android.network.RestApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(
            context.getString(R.string.wfr_preferences),
            Context.MODE_PRIVATE
        )

    @Provides
    fun provideBaseUrl() = BASE_URL

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val securityInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("WFR-Auth-Token", BuildConfig.wfrAuthToken).build()
            chain.proceed(request)
        }


        return OkHttpClient.Builder().apply {
            addInterceptor(securityInterceptor)
            followSslRedirects(false)

            if (BuildConfig.DEBUG) {
                val redactText = Regex("\"password\":\".+\"")
                addInterceptor(
                    HttpLoggingInterceptor {
                        val msg = redactText.replace(it, "\"password\":\"******\"")
                        HttpLoggingInterceptor.Logger.DEFAULT.log(msg)
                    }.apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
            }
        }.build()
    }

    @Singleton
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(LocalDateAdapter())
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient, BASE_URL: String): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): RestApiService =
        retrofit.create(RestApiService::class.java)

    @DefaultDispatcher
    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher