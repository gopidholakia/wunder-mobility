package com.wunder.rental.di.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wunder.rental.BuildConfig.*
import com.wunder.rental.di.annotation.QuickRentDataSourceAnnotation
import com.wunder.rental.di.annotation.WunderDataSourceAnnotation
import com.wunder.rental.remote.Api
import com.wunder.rental.remote.QuickRentApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    /**
     * Provides the service implementation.
     * @param retrofit the Retrofit object used to instantiate the service
     * @return the Post service implementation.
     */
    @Provides
    internal fun provideApi(@WunderDataSourceAnnotation retrofit: Retrofit): Api {
        return retrofit.create(Api::class.java)
    }

    /**
     * Provides the Retrofit object.
     * @return the Retrofit object
     */
    @Provides
    @WunderDataSourceAnnotation
    internal fun provideRetrofit(@ApplicationContext context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(provideDateGsonBuilder()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(provideOkHttpClient(context))
            .build()
    }

    @Provides
    internal fun provideQuickRentApi(@QuickRentDataSourceAnnotation retrofit: Retrofit): QuickRentApi {
        return retrofit.create(QuickRentApi::class.java)
    }

    @Provides
    @QuickRentDataSourceAnnotation
    internal fun provideAuthRetrofit(@ApplicationContext context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(QUICK_RENT_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(provideDateGsonBuilder()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(provideAuthOkHttpClient(context))
            .build()
    }

    @Provides
    internal fun provideOkHttpClient(context: Context): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(provideNetworkInterceptor(context))
        if (DEBUG) {
            httpClient.addInterceptor(provideHttpLoggingInterceptor())
        }
        return httpClient.build()
    }

    @Provides
    internal fun provideAuthOkHttpClient(context: Context): OkHttpClient {
        val httpClient = OkHttpClient.Builder().addInterceptor {
            val request: Request = it.request().newBuilder()
                .header(
                    "Authorization", "Bearer $QUICK_RENT_AUTH_TOKEN"
                )
                .build()
            it.proceed(request)
        }
        httpClient.addInterceptor(provideNetworkInterceptor(context))
        if (DEBUG) {
            httpClient.addInterceptor(provideHttpLoggingInterceptor())
        }
        return httpClient.build()
    }

    @Provides
    @Singleton
    internal fun provideDateGsonBuilder(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create()
    }

    @Provides
    @Singleton
    internal fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }

    @Provides
    @Singleton
    internal fun provideNetworkInterceptor(context: Context): NetworkConnectionInterceptor {
        return NetworkConnectionInterceptor(context)
    }

}