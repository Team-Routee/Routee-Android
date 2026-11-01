import com.routee.android.buildlogic.extension.localProperty

plugins {
    alias(libs.plugins.routee.android.feature.impl)
}

android {
    namespace = "com.routee.android.feature.onboarding"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperty("google.web.client.id")}\"",
        )
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)

    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.workout.api)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
