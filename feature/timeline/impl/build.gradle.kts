plugins {
    alias(libs.plugins.routee.android.feature.impl)
}

android {
    namespace = "com.routee.android.feature.timeline"
}

dependencies {
    implementation(projects.feature.timeline.api)
}
