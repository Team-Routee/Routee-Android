plugins {
    alias(libs.plugins.routee.android.library.compose)
}

android {
    namespace = "com.routee.android.core.map"
}

dependencies {
    implementation(projects.core.designsystem)

    implementation(libs.coil.compose)
    implementation(libs.naver.map.sdk)
}
