plugins {
    alias(libs.plugins.routee.android.feature.impl)
}

android {
    namespace = "com.routee.android.feature.workout"
}

dependencies {
    implementation(projects.feature.workout.api)
    implementation(projects.feature.timeline.api)
    implementation(projects.core.domain)
    implementation(projects.core.map)

    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
