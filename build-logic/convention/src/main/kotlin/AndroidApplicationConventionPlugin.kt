import com.android.build.api.dsl.ApplicationExtension
import com.routee.android.buildlogic.configure.configureKotlinAndroid
import com.routee.android.buildlogic.extension.libs
import com.routee.android.buildlogic.extension.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libs.version("targetSdk")
            }
        }
    }
}
