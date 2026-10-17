package com.routee.android.core.network.di

import com.routee.android.core.network.BuildConfig
import com.routee.android.core.network.authenticator.TokenAuthenticator
import com.routee.android.core.network.extension.toPrettyJsonOrSelf
import com.routee.android.core.network.interceptor.AuthInterceptor
import com.routee.android.core.network.interceptor.TimeZoneInterceptor
import com.routee.android.core.network.token.ReissueService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    private const val CONTENT_TYPE = "application/json"
    private const val LOGGING_TAG = "OkHttp"

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
            Timber.tag(LOGGING_TAG).d(message.toPrettyJsonOrSelf())
        }.apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

    @NoAuth
    @Provides
    @Singleton
    fun provideNoAuthOkHttpClient(
        timeZoneInterceptor: TimeZoneInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(timeZoneInterceptor)
        .build()

    /**
     * [NoAuth] 클라이언트의 커넥션 풀과 Dispatcher를 공유하고 [AuthInterceptor], [TokenAuthenticator]를 추가한다
     */
    @Auth
    @Provides
    @Singleton
    fun provideAuthOkHttpClient(
        @NoAuth client: OkHttpClient,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient = client.newBuilder()
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
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

    /**
     * 재발급 요청은 별도 [Dispatcher]로 보낸다. 401을 받은 요청들이 재발급을 기다리며 Dispatcher 슬롯을 모두 잡고 있어도 재발급 요청이 실행되도록 한다
     */
    @Provides
    @Singleton
    fun provideReissueService(
        @NoAuth client: OkHttpClient,
        converterFactory: Converter.Factory,
    ): ReissueService = createRetrofit(
        client = client.newBuilder().dispatcher(Dispatcher()).build(),
        converterFactory = converterFactory,
    ).create()

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
