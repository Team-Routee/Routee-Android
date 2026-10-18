plugins {
    alias(libs.plugins.routee.android.library.compose)
}

android {
    namespace = "com.routee.android.core.map"
}

dependencies {
    api(libs.naver.map.sdk)
}
