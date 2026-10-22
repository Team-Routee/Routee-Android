plugins {
    alias(libs.plugins.routee.android.feature.impl)
}

android {
    namespace = "com.routee.android.feature.setting"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)

    implementation(projects.feature.setting.api)
    implementation(projects.feature.onboarding.api)
    implementation(libs.timber)
}
