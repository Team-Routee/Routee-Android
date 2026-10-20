plugins {
    alias(libs.plugins.routee.android.feature.impl)
}

android {
    namespace = "com.routee.android.feature.timeline"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.feature.timeline.api)
    implementation(projects.feature.recordEdit.api)
}
