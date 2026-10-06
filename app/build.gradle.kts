import java.util.Properties

val localProperties = Properties().apply {
    val f = project.rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun localProperty(key: String): String = localProperties.getProperty(key)
    ?: error("Missing '$key' in local.properties.")

plugins {
    alias(libs.plugins.routee.android.application)
    alias(libs.plugins.routee.android.compose)
    alias(libs.plugins.routee.hilt)
}

android {
    namespace = "com.routee.android"

    defaultConfig {
        applicationId = "com.routee.android"
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
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
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
