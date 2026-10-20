plugins {
    alias(libs.plugins.routee.android.feature.impl)
}

android {
    namespace = "com.routee.android.feature.timeline"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.map)
    implementation(projects.feature.timeline.api)
    implementation(projects.feature.recordEdit.api)
    implementation(libs.coil.compose)
    implementation(libs.timber)
}
