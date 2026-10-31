package com.routee.android.core.data.connectivity.repository

/** 기기 네트워크 연결 상태. API를 부르지 않는 동작 전에 오프라인인지 확인할 때 사용 */
interface ConnectivityRepository {
    /** 인터넷이 되는 네트워크에 연결되어 있는지. 네트워크를 검증하는 중이면 잠시 기다린 뒤 결과를 반환 */
    suspend fun isConnected(): Boolean
}
