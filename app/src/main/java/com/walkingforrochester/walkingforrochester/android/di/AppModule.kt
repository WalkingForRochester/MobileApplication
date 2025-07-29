package com.walkingforrochester.walkingforrochester.android.di

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.squareup.moshi.Moshi
import com.walkingforrochester.walkingforrochester.android.BuildConfig
import com.walkingforrochester.walkingforrochester.android.LocalDateAdapter
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
                        level = HttpLoggingInterceptor.Level.BASIC
                    }
                )
            }
        }.build()
    }

    @Singleton
    @Provides
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        @IODispatcher ioDispatcher: CoroutineDispatcher
    ): ImageLoader {
        return ImageLoader.Builder(context).apply {
            interceptorCoroutineContext(ioDispatcher)
            components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            // Coil has a cache, so don't double
                            // cache images
                            okHttpClient.newBuilder()
                                .cache(null)
                                .build()
                        }
                    )
                )
            }

            //if (BuildConfig.DEBUG) {
            //    logger(DebugLogger(Logger.Level.Verbose))
            //}
        }.build()
    }

    @Singleton
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(LocalDateAdapter())
            .build()
    }

    @Singleton
    @Provides
    fun provideRestApiService(moshi: Moshi, okHttpClient: OkHttpClient): RestApiService {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(RestApiService.BASE_URL)
            .client(okHttpClient)
            .build()
            .create(RestApiService::class.java)
    }

    @DefaultDispatcher
    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IODispatcher
    @Provides
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    companion object {
        const val PREFERENCE_FILE = "walking_for_rochester_preferences"
    }
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IODispatcher