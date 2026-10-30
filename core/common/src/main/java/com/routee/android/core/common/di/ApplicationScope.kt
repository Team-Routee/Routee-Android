package com.routee.android.core.common.di

import javax.inject.Qualifier

/**
 * 화면 생명주기와 상관없이 앱 프로세스 동안 유지되는 CoroutineScope
 *
 * 화면을 벗어나도 끝까지 실행해야 하는 작업에 주입해 사용한다
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
