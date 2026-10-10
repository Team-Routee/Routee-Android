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
        "implementation"(libs.findBundle("compose").get())
        "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())

        "androidTestImplementation"(platform(bom))
    }
}
