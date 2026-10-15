plugins {
    alias(libs.plugins.routee.android.library.compose)
}

android {
    namespace = "com.routee.android.core.ui"
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.compose)
}
