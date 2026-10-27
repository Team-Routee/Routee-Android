plugins {
    alias(libs.plugins.routee.android.library)
    alias(libs.plugins.routee.hilt)
}

android {
    namespace = "com.routee.android.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
