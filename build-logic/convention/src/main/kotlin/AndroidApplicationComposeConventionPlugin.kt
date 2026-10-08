import com.android.build.api.dsl.ApplicationExtension
import com.routee.android.buildlogic.configure.configureAndroidCompose
import com.routee.android.buildlogic.extension.libs
import com.routee.android.buildlogic.extension.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("routee-android-application"))
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            configureAndroidCompose(extensions.getByType<ApplicationExtension>())
        }
    }
}
