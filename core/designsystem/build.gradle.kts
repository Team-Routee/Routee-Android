plugins {
    alias(libs.plugins.routee.android.library.compose)
}

android {
    namespace = "com.routee.android.core.designsystem"
}

dependencies {
    implementation(projects.core.common)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.android.image.cropper)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
