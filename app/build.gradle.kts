import java.util.Properties

val localProperties = Properties().apply {
    val f = project.rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun localProperty(key: String): String = localProperties.getProperty(key)
    ?: error("Missing '$key' in local.properties.")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.routee.android"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.routee.android"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
