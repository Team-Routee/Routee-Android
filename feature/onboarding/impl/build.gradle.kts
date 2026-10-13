plugins {
    alias(libs.plugins.routee.android.feature.impl)
}

android {
    namespace = "com.routee.android.feature.onboarding"
}

dependencies {
    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.workout.api)
}
