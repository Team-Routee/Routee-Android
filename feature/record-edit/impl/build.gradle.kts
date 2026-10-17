plugins {
    alias(libs.plugins.routee.android.feature.impl)
}

android {
    namespace = "com.routee.android.feature.recordedit"
}

dependencies {
    implementation(projects.feature.recordEdit.api)
    implementation(projects.feature.workout.api)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.coil.compose)
}
