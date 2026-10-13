plugins {
    alias(libs.plugins.routee.android.feature.impl)
}

android {
    namespace = "com.routee.android.feature.workout"
}

dependencies {
    implementation(projects.feature.workout.api)
}
