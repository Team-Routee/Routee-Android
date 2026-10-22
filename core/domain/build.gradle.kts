plugins {
    alias(libs.plugins.routee.android.library)
    alias(libs.plugins.routee.hilt)
}

android {
    namespace = "com.routee.android.core.domain"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
