package com.drivesmart.cx.di

import com.drivesmart.cx.data.remote.micodus.MicodusApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    .header("X-Requested-With", "XMLHttpRequest") // Indispensable para MiCODUS
                    .header("Origin", "https://www.micodus.net")
                    .header("Referer", "https://www.micodus.net/map.aspx") 
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    .build()
                chain.proceed(request)
            }
            .cookieJar(object : okhttp3.CookieJar {
                override fun saveFromResponse(url: okhttp3.HttpUrl, cookies: List<okhttp3.Cookie>) {
                    val cookieManager = android.webkit.CookieManager.getInstance()
                    for (cookie in cookies) {
                        cookieManager.setCookie(url.toString(), cookie.toString())
                    }
                    cookieManager.flush()
                }
                override fun loadForRequest(url: okhttp3.HttpUrl): List<okhttp3.Cookie> {
                    val cookieManager = android.webkit.CookieManager.getInstance()
                    // Buscamos cookies tanto para el subdominio como para la raíz
                    val cookieString = cookieManager.getCookie("https://www.micodus.net") ?: ""
                    val cookieString2 = cookieManager.getCookie("https://micodus.net") ?: ""
                    val fullCookies = (cookieString + "; " + cookieString2).trim()
                    
                    return fullCookies.split(";").mapNotNull {
                        if (it.isBlank()) null else okhttp3.Cookie.parse(url, it.trim())
                    }
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideMicodusRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.micodus.net/") 
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMicodusApiService(retrofit: Retrofit): MicodusApiService {
        return retrofit.create(MicodusApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGson(): com.google.gson.Gson {
        return com.google.gson.GsonBuilder()
            .setLenient() 
            .create()
    }
}
