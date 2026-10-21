plugins {
    alias(libs.plugins.routee.android.library)
    alias(libs.plugins.routee.hilt)
}

android {
    namespace = "com.routee.android.core.domain"
}

dependencies {
    testImplementation(libs.junit)
}
