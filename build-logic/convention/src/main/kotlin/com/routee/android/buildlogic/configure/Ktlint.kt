package com.routee.android.buildlogic.configure

import com.routee.android.buildlogic.extension.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/**
 * ktlint 엔진 버전과 Compose 규칙(compose-rules) 설정
 */
internal fun Project.configureKtlint() {
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")

    extensions.configure<KtlintExtension> {
        version.set(libs.findVersion("ktlintEngine").get().requiredVersion)
    }

    dependencies {
        "ktlintRuleset"(libs.findLibrary("compose-rules-ktlint").get())
    }
}
