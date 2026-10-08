package com.routee.android.buildlogic.configure

import com.android.build.api.dsl.CommonExtension
import com.routee.android.buildlogic.extension.libs
import com.routee.android.buildlogic.extension.version
import org.gradle.api.JavaVersion
import org.gradle.api.Project

internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    commonExtension.apply {
        compileSdk = libs.version("compileSdk")

        defaultConfig.apply {
            minSdk = libs.version("minSdk")
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions.apply {
            val jvmTarget = JavaVersion.toVersion(libs.findVersion("jvmTarget").get().requiredVersion)
            sourceCompatibility = jvmTarget
            targetCompatibility = jvmTarget
        }
    }
}
