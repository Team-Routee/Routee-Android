plugins {
    alias(libs.plugins.routee.android.feature.impl)
}

android {
    namespace = "com.routee.android.feature.timeline"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.map)
    implementation(projects.core.network)
    implementation(projects.feature.timeline.api)
    implementation(projects.feature.recordEdit.api)
    implementation(libs.coil.compose)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
