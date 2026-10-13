# Routee Android Docs

| Working on | Read |
| --- | --- |
| Modules, dependencies, file locations | [modularization](architecture/modularization.md) |
| NavKey, entries, moving between screens, tabs | [navigation](architecture/navigation.md) |
| Repository, Service, DTO, DataStore, Retrofit qualifiers | [data-layer](architecture/data-layer.md) |
| Failures, server error codes, logging | [error-handling](architecture/error-handling.md) |
| Status bar, navigation bar, tab bar, keyboard padding | [window-insets](conventions/window-insets.md) |
| Drawable and string names | [resources](conventions/resources.md) |

| Folder | Holds |
| --- | --- |
| `architecture/` | How the code is organized |
| `conventions/` | How to write UI and resources |

## Writing docs

- 제목, 표, 코드. 설명은 코드 주석으로.
- 제목과 표는 영어, 주석은 한국어.
- 한 주제는 한 문서에만. 확정된 규칙만 쓰고, 제안은 이슈와 PR에서.
- 규칙을 바꾸면 같은 PR에서 `AGENTS.md`나 docs도 고친다. 필요 없어진 규칙은 지운다.
