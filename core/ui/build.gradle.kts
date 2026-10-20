plugins {
    alias(libs.plugins.routee.android.library.compose)
}

android {
    namespace = "com.routee.android.core.ui"
}

dependencies {
    implementation(projects.core.designsystem)

    implementation(libs.androidx.activity.compose)
    implementation(libs.lottie.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
