# Resources

## Drawables

| Prefix | Type | Pattern | Example |
| --- | --- | --- | --- |
| `ic_` | XML vector | `ic_<name>_<size>_<color>` | `ic_edit_sm_line_grey` |
| `img_` | WebP, PNG | `img_<name>` | `img_profile_default` |
| `graphic_` | XML illustration | `graphic_<name>_<size>` | `graphic_empty_data_xl` |
| `routee_` | Logo, brand | `routee_<name>` | `routee_logo_mint_md` |

Sizes: `sm`, `md`, `lg`, `xl`.

## Strings

```xml
<!-- 기록 편집 메인 -->
<string name="record_edit_main_empty_title">기록이 없어요.</string>
<string name="record_edit_main_recap_title_edit_a11y">기록 제목 편집</string>
<plurals name="record_edit_main_recap_count">
    <item quantity="other">기록 %1$d개</item>
</plurals>
```

| Item | Rule |
| --- | --- |
| Name | `<feature>_<screen>_<area>_<what>`, snake_case |
| Accessibility | Ends with `_a11y` (`contentDescription`) |
| Location | Feature `impl/src/main/res/values/strings.xml`, app-wide in `app` |
| Grouping | Under a `<!-- screen name -->` comment |
| Numbers | `%1$d`, `plurals` when the count changes the text |
| Reuse | Reuse the same meaning, delete unused strings |
| Scope | User-facing text only. Not logs, keys, test data, Preview samples |

| Don't | Do |
| --- | --- |
| `Text("기록이 없어요.")` | `Text(stringResource(R.string.record_edit_main_empty_title))` |
| `contentDescription = "닫기"` | `contentDescription = stringResource(R.string.<..>_close_a11y)` |
| `"기록 " + count + "개"` | `pluralStringResource(R.plurals.<..>, count, count)` |
