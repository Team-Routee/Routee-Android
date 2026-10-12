plugins {
    alias(libs.plugins.routee.android.feature)
}

android {
    namespace = "com.routee.android.feature.recordedit"
}

dependencies {
    implementation(libs.kotlinx.collections.immutable)
}
