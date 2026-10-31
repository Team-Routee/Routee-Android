package com.routee.android.core.data.permission.repository

/** 권한 거부 기록. 거부한 뒤에는 시스템 팝업 대신 안내 모달을 띄울 때 사용 */
interface PermissionRepository {
    /** 운동 메인 첫 진입 팝업에서 위치 권한을 거부한 적이 있는지 */
    suspend fun getLocationPermissionDenied(): Result<Boolean>

    suspend fun saveLocationPermissionDenied(): Result<Unit>

    /** 이번 앱 실행에서 위치 권한 안내를 처음 띄우는지. 처음이면 true, 이후 false */
    fun consumeLocationGuideOnLaunch(): Boolean
}
