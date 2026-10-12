package com.routee.android.core.network.di

import com.routee.android.core.network.BuildConfig
import com.routee.android.core.network.constant.AUTHORIZATION
import com.routee.android.core.network.interceptor.AuthInterceptor
import com.routee.android.core.network.interceptor.TimeZoneInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    private const val CONTENT_TYPE = "application/json"
    private const val LOGGING_TAG = "OkHttp"
    private const val MASKED_VALUE = "****"
    private val TokenFieldRegex = Regex("\"(\\w*[Tt]oken)\"\\s*:\\s*\"[^\"]*\"")

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideConverterFactory(json: Json): Converter.Factory =
        json.asConverterFactory(CONTENT_TYPE.toMediaType())

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor { message ->
            Timber.tag(LOGGING_TAG).d(message.replace(TokenFieldRegex, "\"$1\":\"$MASKED_VALUE\""))
        }.apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            redactHeader(AUTHORIZATION)
        }

    @NoAuth
    @Provides
    @Singleton
    fun provideNoAuthOkHttpClient(
        timeZoneInterceptor: TimeZoneInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(timeZoneInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    /**
     * [NoAuth] 클라이언트의 커넥션 풀과 Dispatcher를 공유하고, 인터셉터 맨 앞에 [AuthInterceptor]를 추가한다
     */
    @Auth
    @Provides
    @Singleton
    fun provideAuthOkHttpClient(
        @NoAuth client: OkHttpClient,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient = client.newBuilder()
        .apply { interceptors().add(0, authInterceptor) }
        .build()

    @NoAuth
    @Provides
    @Singleton
    fun provideNoAuthRetrofit(
        @NoAuth client: OkHttpClient,
        converterFactory: Converter.Factory,
    ): Retrofit = createRetrofit(client, converterFactory)

    @Auth
    @Provides
    @Singleton
    fun provideAuthRetrofit(
        @Auth client: OkHttpClient,
        converterFactory: Converter.Factory,
    ): Retrofit = createRetrofit(client, converterFactory)

    private fun createRetrofit(
        client: OkHttpClient,
        converterFactory: Converter.Factory,
    ): Retrofit {
        require(BuildConfig.BASE_URL.isNotEmpty()) {
            "local.properties에 test.base.url, prod.base.url을 설정해주세요."
        }
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .build()
    }
}
