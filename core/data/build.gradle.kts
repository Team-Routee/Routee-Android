plugins {
    alias(libs.plugins.routee.android.library)
    alias(libs.plugins.routee.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.routee.android.core.data"
}

dependencies {
    implementation(projects.core.network)
}
