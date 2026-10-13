package com.routee.android.core.network.di

import com.routee.android.core.network.token.TokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 토큰 저장소가 구현되기 전까지 사용하는 임시 바인딩. 항상 토큰이 없는 상태로 동작한다
 *
 * 토큰 저장소 구현 시 이 모듈을 제거하고 구현체를 [TokenProvider]로 바인딩한다
 */
@Module
@InstallIn(SingletonComponent::class)
internal object TokenProviderModule {
    @Provides
    @Singleton
    fun provideTokenProvider(): TokenProvider = object : TokenProvider {
        override suspend fun getAccessToken(): String? = null
    }
}
