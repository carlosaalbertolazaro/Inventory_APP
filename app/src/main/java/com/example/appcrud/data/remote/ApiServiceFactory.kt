package com.example.appcrud.data.remote

import android.content.Context
import com.example.appcrud.BuildConfig
import com.example.appcrud.data.local.SessionStorage
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiServiceFactory private constructor(
    private val retrofit: Retrofit
) {
    fun create(): ApiService = retrofit.create(ApiService::class.java)

    companion object {
        fun create(context: Context): ApiServiceFactory {
            val sessionStorage = SessionStorage.getInstance(context.applicationContext)
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val token = sessionStorage.getToken()
                    val request = if (token.isNullOrBlank()) {
                        chain.request()
                    } else {
                        chain.request().newBuilder()
                            .header("Authorization", "Bearer $token")
                            .build()
                    }
                    chain.proceed(request)
                }
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return ApiServiceFactory(retrofit)
        }
    }
}
