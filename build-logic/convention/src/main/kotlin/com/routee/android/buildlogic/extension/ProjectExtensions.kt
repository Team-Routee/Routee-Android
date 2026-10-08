package com.routee.android.buildlogic.extension

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.version(alias: String): Int =
    findVersion(alias).get().requiredVersion.toInt()

fun VersionCatalog.pluginId(alias: String): String =
    findPlugin(alias).get().get().pluginId
