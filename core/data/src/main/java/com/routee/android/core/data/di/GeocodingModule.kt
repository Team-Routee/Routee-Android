package com.routee.android.core.data.di

import com.routee.android.core.data.BuildConfig
import com.routee.android.core.data.geocoding.remote.service.NaverMapsService
import com.routee.android.core.data.geocoding.repository.ReverseGeocodingRepository
import com.routee.android.core.data.geocoding.repository.ReverseGeocodingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
internal abstract class GeocodingModule {
    @Binds
    @Singleton
    abstract fun bindReverseGeocodingRepository(impl: ReverseGeocodingRepositoryImpl): ReverseGeocodingRepository

    companion object {
        private const val NAVER_MAPS_BASE_URL = "https://maps.apigw.ntruss.com/"

        @Provides
        @Singleton
        fun provideNaverMapsService(converterFactory: Converter.Factory): NaverMapsService {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .header("x-ncp-apigw-api-key-id", BuildConfig.NAVER_MAP_CLIENT_ID)
                            .header("x-ncp-apigw-api-key", BuildConfig.NAVER_MAP_CLIENT_SECRET)
                            .build(),
                    )
                }
                .build()
            return Retrofit.Builder()
                .baseUrl(NAVER_MAPS_BASE_URL)
                .client(client)
                .addConverterFactory(converterFactory)
                .build()
                .create()
        }
    }
}
