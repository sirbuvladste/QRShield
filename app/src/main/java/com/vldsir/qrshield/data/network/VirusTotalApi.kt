package com.vldsir.qrshield.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.ConnectionSpec
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface VirusTotalApi {

    @GET("api/v3/urls/{id}")
    suspend fun getUrl(
        @Path("id") id: String,
        @Header("x-apikey") apiKey: String,
    ): Response<VirusTotalUrlResponse>

    @FormUrlEncoded
    @POST("api/v3/urls")
    suspend fun submitUrl(
        @Field("url") url: String,
        @Header("x-apikey") apiKey: String,
    ): Response<VirusTotalSubmitResponse>
}

object VirusTotalApiFactory {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun create(): VirusTotalApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.NONE })
            .build()

        return Retrofit.Builder()
            .baseUrl("https://www.virustotal.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(VirusTotalApi::class.java)
    }
}
