package com.routee.android.buildlogic.configure

import com.android.build.api.dsl.CommonExtension
import com.routee.android.buildlogic.extension.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(commonExtension: CommonExtension) {
    commonExtension.apply {
        buildFeatures.compose = true
    }

    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        "implementation"(platform(bom))
        "implementation"(libs.findLibrary("androidx-compose-ui").get())
        "implementation"(libs.findLibrary("androidx-compose-ui-graphics").get())
        "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        "implementation"(libs.findLibrary("androidx-compose-material3").get())
        "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())

        "androidTestImplementation"(platform(bom))
    }
}
