package com.routee.android.core.network.di

import javax.inject.Qualifier

/**
 * Access Token이 필요한 API용 OkHttpClient / Retrofit
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Auth

/**
 * 로그인, 토큰 재발급처럼 Access Token 없이 호출하는 API용 OkHttpClient / Retrofit
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NoAuth
