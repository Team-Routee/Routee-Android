import com.routee.android.buildlogic.extension.localProperty

plugins {
    alias(libs.plugins.routee.android.library)
    alias(libs.plugins.routee.hilt)
    alias(libs.plugins.kotlin.serialization)
}

fun baseUrl(key: String): String = localProperty(key).let { url ->
    if (url.isEmpty() || url.endsWith("/")) url else "$url/"
}

android {
    namespace = "com.routee.android.core.network"

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"${baseUrl("test.base.url")}\"")
        }

        release {
            buildConfigField("String", "BASE_URL", "\"${baseUrl("prod.base.url")}\"")
        }
    }
}

dependencies {
    api(libs.retrofit.core)
    api(libs.kotlinx.serialization.json)

    implementation(projects.core.common)

    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
