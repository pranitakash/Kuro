package com.kuro.music.di

import com.kuro.music.data.remote.InnertubeClient
import com.kuro.music.data.remote.PipedApiService
import com.kuro.music.data.remote.PipedInstanceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val DEFAULT_PIPED_INSTANCE = "https://api.piped.private.coffee"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DEFAULT_PIPED_INSTANCE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePipedApiService(retrofit: Retrofit): PipedApiService {
        return retrofit.create(PipedApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePipedInstanceManager(okHttpClient: OkHttpClient): PipedInstanceManager {
        return PipedInstanceManager(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideInnertubeClient(okHttpClient: OkHttpClient): InnertubeClient {
        return InnertubeClient(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideLrcLibService(okHttpClient: OkHttpClient): com.kuro.music.data.remote.LrcLibService {
        return Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(com.kuro.music.data.remote.LrcLibService::class.java)
    }
}
