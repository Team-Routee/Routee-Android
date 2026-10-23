plugins {
    alias(libs.plugins.routee.android.feature.impl)
}

android {
    namespace = "com.routee.android.feature.archive"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.network)
    implementation(projects.feature.archive.api)
    implementation(projects.core.map)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
