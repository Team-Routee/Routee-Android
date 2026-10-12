import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.routee.android.library)
    alias(libs.plugins.routee.hilt)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun baseUrl(key: String): String = localProperties.getProperty(key, "").let { url ->
    if (url.isEmpty() || url.endsWith("/")) url else "$url/"
}

fun baseUrlKey(buildType: String?): String = if (buildType == "release") "prod.base.url" else "test.base.url"

/**
 * BASE_URL 호스트에만 평문(HTTP) 통신을 허용하는 network_security_config를 생성한다
 *
 * 서버 주소를 레포에 올리지 않기 위해 local.properties 값으로 빌드 시점에 만든다. HTTPS 전환 시 제거
 */
abstract class GenerateNetworkSecurityConfigTask : DefaultTask() {
    @get:Input
    abstract val cleartextHost: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val host = cleartextHost.get()
        val domainConfig = if (host.isEmpty()) {
            ""
        } else {
            """
            |    <domain-config cleartextTrafficPermitted="true">
            |        <domain includeSubdomains="false">$host</domain>
            |    </domain-config>
            |
            """.trimMargin()
        }
        val file = outputDir.file("xml/network_security_config.xml").get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |<?xml version="1.0" encoding="utf-8"?>
            |<network-security-config>
            |    <base-config cleartextTrafficPermitted="false" />
            |$domainConfig</network-security-config>
            |
            """.trimMargin(),
        )
    }
}

android {
    namespace = "com.routee.android.core.network"

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"${baseUrl(baseUrlKey("debug"))}\"")
        }

        release {
            buildConfigField("String", "BASE_URL", "\"${baseUrl(baseUrlKey("release"))}\"")
        }
    }
}

androidComponents {
    onVariants { variant ->
        val host = baseUrl(baseUrlKey(variant.buildType)).takeIf { it.isNotEmpty() }?.let { URI(it).host }.orEmpty()
        val taskName = "generate${variant.name.replaceFirstChar { it.uppercase() }}NetworkSecurityConfig"
        val task = tasks.register<GenerateNetworkSecurityConfigTask>(taskName) {
            cleartextHost.set(host)
        }
        variant.sources.res?.addGeneratedSourceDirectory(task, GenerateNetworkSecurityConfigTask::outputDir)
    }
}

dependencies {
    api(libs.retrofit.core)
    api(libs.kotlinx.serialization.json)

    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
