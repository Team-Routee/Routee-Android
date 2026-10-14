# Data Layer

## Files

| Path under `core/data/.../<domain>/` | Example |
| --- | --- |
| `model/` | `LoginResult` |
| `exception/` | `AuthException` |
| `remote/service/` | `AuthService` |
| `remote/dto/request\|response/` | `LoginRequest`, `TokenResponse` |
| `local/` | `TokenLocalDataSource` |
| `repository/` | `AuthRepository`, `AuthRepositoryImpl` |

Hilt modules live in `core/data/.../di/`.

## Service and DTO

```kotlin
internal interface AuthService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): BaseResponse<TokenResponse>
}

@Serializable
internal data class TokenResponse( // DTO는 internal, core:data 밖으로 안 나감
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
)
```

## Repository

```kotlin
// 함수 이름
interface RecapRepository {
    suspend fun getRecap(id: Long): Result<Recap>           // get: 1회 조회
    fun observeRecaps(): Flow<List<Recap>>                    // observe: Flow로 계속 구독
    suspend fun saveRecap(recap: Recap): Result<Unit>         // save, update: 저장, 수정
    suspend fun deleteRecap(id: Long): Result<Unit>           // delete: 하나, clear: 전부
    suspend fun shareRecap(id: Long): Result<Unit>            // 그 외 동작은 도메인 동사 (login, register)
}                                                             // post, put, patch는 Service에만

// 구현 (core/data/.../auth)
internal class AuthRepositoryImpl @Inject constructor( // 구현체는 internal
    private val authService: AuthService,
    private val apiResponseHandler: ApiResponseHandler,
) : AuthRepository {
    override suspend fun login(idToken: String, provider: SocialProvider): Result<LoginResult> =
        apiResponseHandler.safeApiCall { authService.login(LoginRequest(idToken, provider.name)) } // runCatching 대신
            .mapApiException(::mapAuthException)
}
```

## Retrofit

| Qualifier | Access token | TimeZone header | For |
| --- | --- | --- | --- |
| `@Auth` | Yes | Yes | Most APIs (default) |
| `@NoAuth` | No | Yes | Login, sign-up, token reissue |

```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal abstract class RecapModule {
    @Binds @Singleton
    abstract fun bindRecapRepository(impl: RecapRepositoryImpl): RecapRepository

    companion object {
        @Provides @Singleton
        fun provideRecapService(@Auth retrofit: Retrofit): RecapService = retrofit.create()
    }
}
```
