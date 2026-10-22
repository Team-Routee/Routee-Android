# Routee Android

Workout recap app. Kotlin, Jetpack Compose, single activity, Navigation 3, Hilt.
Versions in `gradle/libs.versions.toml`. AGP 9 (new DSL), minSdk 26.
Modules: `app`, `core/{common,designsystem,domain,map,navigation,ui,data,database,network}`, `feature/<x>/{api,impl}`.
Commands: compile `./gradlew :<module>:compileDebugKotlin`, format `ktlintFormat`, lint `ktlintCheck`, build `assembleDebug`.

## Before editing

Find the area in [docs/index.md](docs/index.md) and read that page first.

## Comments

KDoc only on public API (Korean noun phrase, how to use it). `//` only for short labels, `TODO`, and Figma labels. No comments that repeat the code.

## Security (public repository)

Never commit `local.properties`, `keystore/`, `google-services.json`, or API keys.
Do not read or print secrets.
