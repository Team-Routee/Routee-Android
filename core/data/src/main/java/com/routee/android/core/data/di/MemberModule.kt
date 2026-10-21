package com.routee.android.core.data.di

import com.routee.android.core.data.member.remote.service.MemberService
import com.routee.android.core.data.member.repository.MemberRepository
import com.routee.android.core.data.member.repository.MemberRepositoryImpl
import com.routee.android.core.network.di.Auth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MemberModule {
    @Binds
    @Singleton
    abstract fun bindMemberRepository(memberRepositoryImpl: MemberRepositoryImpl): MemberRepository

    companion object {
        @Provides
        @Singleton
        fun provideMemberService(
            @Auth retrofit: Retrofit,
        ): MemberService = retrofit.create()
    }
}
