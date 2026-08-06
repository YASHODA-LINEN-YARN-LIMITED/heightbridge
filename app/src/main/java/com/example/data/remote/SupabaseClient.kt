package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.remote.SupabaseApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object SupabaseClient {
    private const val FALLBACK_URL = "https://lxuapkccxaadwixjpirs.supabase.co/rest/v1/"
    private const val FALLBACK_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx4dWFwa2NjeGFhZHdpeGpwaXJzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzg4MzQ4NDksImV4cCI6MjA5NDQxMDg0OX0.rzjJFNOb1gx0Z4cMSfkW9yDe4rI8oO6TLTzcVXswPek"

    val baseUrl: String
        get() {
            return try {
                val url = BuildConfig.SUPABASE_URL
                if (!url.isNull_or_empty()) url else FALLBACK_URL
            } catch (e: Throwable) {
                FALLBACK_URL
            }
        }

    val anonKey: String
        get() {
            return try {
                val key = BuildConfig.SUPABASE_ANON_KEY
                if (!key.isNull_or_empty()) key else FALLBACK_ANON_KEY
            } catch (e: Throwable) {
                FALLBACK_ANON_KEY
            }
        }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val key = anonKey
        val requestBuilder = original.newBuilder()
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: SupabaseApi by lazy {
        Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseApi::class.java)
    }
}
