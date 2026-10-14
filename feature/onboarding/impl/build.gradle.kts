import java.util.Properties

plugins {
    alias(libs.plugins.routee.android.feature.impl)
}

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
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
            "\"${localProperties.getProperty("google.web.client.id", "")}\"",
        )
    }
}

dependencies {
    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.workout.api)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
}
