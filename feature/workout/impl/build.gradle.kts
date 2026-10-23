plugins {
    alias(libs.plugins.routee.android.feature.impl)
}

android {
    namespace = "com.routee.android.feature.workout"
}

dependencies {
    implementation(projects.feature.workout.api)
    implementation(projects.core.domain)
    implementation(projects.core.map)

    implementation(libs.timber)

    testImplementation(libs.junit)
}
