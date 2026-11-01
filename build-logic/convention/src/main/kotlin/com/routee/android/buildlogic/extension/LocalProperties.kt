package com.routee.android.buildlogic.extension

import java.util.Properties
import org.gradle.api.Project

/** 루트 local.properties 값, 키가 없으면 [defaultValue] */
fun Project.localProperty(key: String, defaultValue: String = ""): String = localProperties().getProperty(key, defaultValue)

/** 루트 local.properties 값, 키가 없으면 빌드 실패 */
fun Project.requireLocalProperty(key: String): String =
    localProperties().getProperty(key) ?: error("Missing '$key' in local.properties.")

private fun Project.localProperties(): Properties = Properties().apply {
    val file = rootProject.layout.projectDirectory.file("local.properties")
    providers.fileContents(file).asText.orNull?.let { load(it.reader()) }
}
