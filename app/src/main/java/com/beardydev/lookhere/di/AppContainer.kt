package com.beardydev.lookhere.di

import android.content.Context
import com.beardydev.lookhere.BuildConfig
import com.beardydev.lookhere.data.klipy.KlipyApi
import com.beardydev.lookhere.data.klipy.KlipyRepository
import com.beardydev.lookhere.data.settings.SelectedGifRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AppContainer(context: Context) {

    val isKlipyApiKeyConfigured: Boolean = BuildConfig.KLIPY_API_KEY.isNotBlank()

    val selectedGifRepository = SelectedGifRepository(context.applicationContext)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
                )
            }
        }
        .build()

    private val klipyApi: KlipyApi = Retrofit.Builder()
        .baseUrl("https://api.klipy.com/api/v1/${BuildConfig.KLIPY_API_KEY}/")
        .client(okHttpClient)
        .addConverterFactory(
            Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())
        )
        .build()
        .create(KlipyApi::class.java)

    val klipyRepository = KlipyRepository(klipyApi)
}
