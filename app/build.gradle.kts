import com.routee.android.buildlogic.extension.requireLocalProperty

plugins {
    alias(libs.plugins.routee.android.application.compose)
    alias(libs.plugins.routee.hilt)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.routee.android"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.routee.android"
        versionName = libs.versions.versionName.get()
        versionCode = versionCodeOf(libs.versions.versionName.get(), libs.versions.versionBuild.get().toInt())
        manifestPlaceholders["naverMapClientId"] = requireLocalProperty("naver.map.client.id")
    }

    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("keystore/routee-debug-key.jks")
            storePassword = requireLocalProperty("debug.store.password")
            keyAlias = requireLocalProperty("debug.key.alias")
            keyPassword = requireLocalProperty("debug.key.password")
        }

        create("release") {
            storeFile = rootProject.file("keystore/routee-release-key.jks")
            storePassword = requireLocalProperty("release.store.password")
            keyAlias = requireLocalProperty("release.key.alias")
            keyPassword = requireLocalProperty("release.key.password")
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
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.domain)
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
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.timber)
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.android.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

fun versionCodeOf(versionName: String, build: Int): Int {
    val parts = versionName.split(".").map { it.toInt() }
    require(parts.size == 3 && parts.drop(1).all { it < 100 } && build < 100) {
        "versionName은 x.y.z, y, z, versionBuild는 99 이하: $versionName, $build"
    }
    val (major, minor, patch) = parts
    return major * 1_000_000 + minor * 10_000 + patch * 100 + build
}
