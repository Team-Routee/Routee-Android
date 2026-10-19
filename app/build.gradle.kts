import java.util.Properties

val localProperties = Properties().apply {
    val f = project.rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun localProperty(key: String): String = localProperties.getProperty(key)
    ?: error("Missing '$key' in local.properties.")

plugins {
    alias(libs.plugins.routee.android.application.compose)
    alias(libs.plugins.routee.hilt)
}

android {
    namespace = "com.routee.android"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.routee.android"
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
        manifestPlaceholders["naverMapClientId"] = localProperty("naver.map.client.id")
    }

    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("keystore/routee-debug-key.jks")
            storePassword = localProperty("debug.store.password")
            keyAlias = localProperty("debug.key.alias")
            keyPassword = localProperty("debug.key.password")
        }

        create("release") {
            storeFile = rootProject.file("keystore/routee-release-key.jks")
            storePassword = localProperty("release.store.password")
            keyAlias = localProperty("release.key.alias")
            keyPassword = localProperty("release.key.password")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.network)
    implementation(projects.core.navigation)
    implementation(projects.core.ui)

    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.onboarding.impl)
    implementation(projects.feature.workout.api)
    implementation(projects.feature.workout.impl)
    implementation(projects.feature.timeline.impl)
    implementation(projects.feature.recordEdit.api)
    implementation(projects.feature.recordEdit.impl)
    implementation(projects.feature.archive.api)
    implementation(projects.feature.archive.impl)
    implementation(projects.feature.setting.api)
    implementation(projects.feature.setting.impl)

    implementation(libs.bundles.navigation3)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.timber)
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.android.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
