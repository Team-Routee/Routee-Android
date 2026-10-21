# Domain Layer

| Item | Rule |
| --- | --- |
| When | Combines 2+ repositories, is reused by 2+ ViewModels or an Android `Service`, or is too complex for one ViewModel |
| Skip | Only passes one Repository call through → the ViewModel calls the Repository directly. Data joined by a Room query → one Repository instead |
| Name | Verb + noun + `UseCase`. One action per class (`ValidateNicknameUseCase`, `GetWorkoutWithPhotosUseCase`) |
| Shape | `class` + `@Inject constructor` + `operator fun invoke` |
| State | No mutable state. No `@Singleton` |
| Depends on | Repositories (`core:data`), other UseCases. No Android or UI types. Repository interfaces stay in `core:data` (Google guide, not Clean Architecture) |
| Used by | ViewModels, Android `Service` (foreground service) |
| Threading | Main-safe. Heavy work (mapping long lists) inside `withContext(Dispatchers.Default)` |
| Location | `core/domain/.../<domain>/`, test in `core/domain/src/test/` |

```kotlin
// 레포지토리 2개 조합 (예시)
class GetWorkoutWithPhotosUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val timelineRepository: TimelineRepository,
) {
    operator fun invoke(workoutId: Long): Flow<WorkoutWithPhotos> =
        combine(workoutRepository.getWorkout(workoutId), timelineRepository.getPhotos(workoutId)) { workout, photos ->
            WorkoutWithPhotos(workout, photos)
        }
}

// 여러 화면이 같이 쓰는 규칙
class ValidateNicknameUseCase @Inject constructor() {
    operator fun invoke(nickname: CharSequence): NicknameValidation = ... // 결과는 도메인 값(EMPTY, VALID, INVALID)으로
}

// ViewModel: UseCase와 Repository를 같이 써도 됨
class ProfileEditViewModel @Inject constructor(
    private val validateNickname: ValidateNicknameUseCase,
    private val memberRepository: MemberRepository, // 한 번 호출로 끝나면 Repository 직접
) : ViewModel()
```

| Don't | Do |
| --- | --- |
| UseCase that only calls one Repository method | Call the Repository from the ViewModel |
| `object NicknamePolicy`, `Util` / `Helper` class | `class` UseCase named by its action |
| Return UI types (`RouteeTextFieldValidation`) | Return domain values, map in the ViewModel |
