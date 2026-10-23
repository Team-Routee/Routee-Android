package com.routee.android.core.data.permission.repository

/** 권한 요청 기록. 처음 한 번만 시스템 팝업을 띄울 때 사용 */
interface PermissionRepository {
    /** 운동 메인 첫 진입 때 위치 권한을 물어본 적이 있는지 */
    suspend fun getLocationPermissionRequested(): Result<Boolean>

    suspend fun saveLocationPermissionRequested(): Result<Unit>
}
