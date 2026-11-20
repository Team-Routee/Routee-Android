import com.routee.android.buildlogic.extension.localProperty

plugins {
    alias(libs.plugins.routee.android.library)
    alias(libs.plugins.routee.hilt)
}

android {
    namespace = "com.routee.android.core.analytics"

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "MIXPANEL_TOKEN", "\"${localProperty("mixpanel.dev.token")}\"")
        }

        release {
            buildConfigField("String", "MIXPANEL_TOKEN", "\"${localProperty("mixpanel.prod.token")}\"")
        }
    }
}

dependencies {
    implementation(projects.core.common)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.mixpanel.android)
    implementation(libs.installreferrer)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
