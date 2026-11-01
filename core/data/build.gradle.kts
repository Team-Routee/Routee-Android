import com.routee.android.buildlogic.extension.localProperty

plugins {
    alias(libs.plugins.routee.android.library)
    alias(libs.plugins.routee.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.routee.android.core.data"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "NAVER_MAP_CLIENT_ID", "\"${localProperty("naver.map.client.id")}\"")
        buildConfigField("String", "NAVER_MAP_CLIENT_SECRET", "\"${localProperty("naver.map.client.secret")}\"")
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.network)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.play.services.location)
    implementation(libs.androidx.core.location.altitude)
    implementation(libs.androidx.exifinterface)
    implementation(libs.coil)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
