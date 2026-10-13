package com.routee.android.core.network.util

import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.network.model.BaseResponse
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ApiResponseHandlerTest {

    private val handler = ApiResponseHandler(Json { ignoreUnknownKeys = true })

    /**
     * GIVEN: 서버가 data를 포함한 성공 응답을 내려줄 때
     * WHEN: safeApiCall로 호출하면
     * THEN: data만 꺼내 성공으로 반환한다
     */
    @Test
    fun `성공 응답이면 data를 반환한다`() = runTest {
        val result = handler.safeApiCall { BaseResponse(status = 200, code = "OK", message = "성공", data = "data") }

        assertEquals("data", result.getOrNull())
    }

    /**
     * GIVEN: 서버가 data 없이 성공 응답을 내려줄 때
     * WHEN: safeApiCall로 호출하면
     * THEN: EmptyData로 실패한다
     */
    @Test
    fun `성공 응답의 data가 null이면 EmptyData로 실패한다`() = runTest {
        val result = handler.safeApiCall<String> { BaseResponse(status = 200, code = "OK", message = "성공") }

        assertTrue(result.exceptionOrNull() is ApiException.EmptyData)
    }

    /**
     * GIVEN: 서버가 401과 에러 바디를 내려줄 때
     * WHEN: safeApiCall로 호출하면
     * THEN: Unauthorized로 실패하고 서버 코드를 담는다
     */
    @Test
    fun `401 응답이면 Unauthorized와 서버 코드를 반환한다`() = runTest {
        val result = handler.safeApiCall<String> {
            throw httpException(statusCode = 401, code = "INVALID_TOKEN")
        }

        val exception = result.exceptionOrNull()
        assertTrue(exception is ApiException.Unauthorized)
        assertEquals("INVALID_TOKEN", (exception as ApiException).serverCode)
    }

    /**
     * GIVEN: 서버가 500을 내려줄 때
     * WHEN: safeApiCall로 호출하면
     * THEN: ServerError로 실패하고 상태 코드를 담는다
     */
    @Test
    fun `5xx 응답이면 ServerError를 반환한다`() = runTest {
        val result = handler.safeApiCall<String> {
            throw httpException(statusCode = 500, code = "INTERNAL_SERVER_ERROR")
        }

        val exception = result.exceptionOrNull()
        assertTrue(exception is ApiException.ServerError)
        assertEquals(500, (exception as ApiException.ServerError).statusCode)
    }

    /**
     * GIVEN: 에러 바디를 해석할 수 없는 404 응답일 때
     * WHEN: safeUnitApiCall로 호출하면
     * THEN: 서버 코드 없이 NotFound로 실패한다
     */
    @Test
    fun `에러 바디를 해석할 수 없어도 상태 코드로 예외를 구분한다`() = runTest {
        val result = handler.safeUnitApiCall {
            throw HttpException(Response.error<Unit>(404, "not json".toResponseBody(JSON_MEDIA_TYPE)))
        }

        val exception = result.exceptionOrNull()
        assertTrue(exception is ApiException.NotFound)
        assertEquals(null, (exception as ApiException).serverCode)
    }

    /**
     * GIVEN: 네트워크 연결에 실패했을 때
     * WHEN: safeApiCall로 호출하면
     * THEN: NetworkConnection으로 실패한다
     */
    @Test
    fun `네트워크 오류면 NetworkConnection을 반환한다`() = runTest {
        val result = handler.safeApiCall<String> { throw IOException("timeout") }

        assertTrue(result.exceptionOrNull() is ApiException.NetworkConnection)
    }

    /**
     * GIVEN: 호출 중 코루틴이 취소되었을 때
     * WHEN: safeApiCall로 호출하면
     * THEN: 실패로 감싸지 않고 취소를 그대로 전파한다
     */
    @Test
    fun `코루틴 취소는 Result로 감싸지 않고 전파한다`() = runTest {
        try {
            handler.safeApiCall<String> { throw CancellationException("cancelled") }
            fail("CancellationException이 전파되어야 한다")
        } catch (_: CancellationException) {
        }
    }

    private fun httpException(statusCode: Int, code: String): HttpException {
        val body = """{"status":$statusCode,"code":"$code","message":"에러","data":null}"""
        return HttpException(Response.error<Any>(statusCode, body.toResponseBody(JSON_MEDIA_TYPE)))
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
