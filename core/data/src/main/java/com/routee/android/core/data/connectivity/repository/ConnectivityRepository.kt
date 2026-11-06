package com.routee.android.core.data.connectivity.repository

import kotlinx.coroutines.flow.Flow

/** 기기 인터넷 연결 상태 조회 및 변경 감지 */
interface ConnectivityRepository {
    /** 인터넷이 되는 네트워크에 연결되어 있는지. 네트워크를 검증하는 중이면 잠시 기다린 뒤 결과를 반환 */
    suspend fun isConnected(): Boolean

    /** 현재 인터넷 연결 상태와 이후 변경 감지, 화면이 보이는 동안 수집 */
    fun observeConnection(): Flow<Boolean>
}
