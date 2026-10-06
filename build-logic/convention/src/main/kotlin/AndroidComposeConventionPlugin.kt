import com.android.build.api.dsl.CommonExtension
import com.routee.android.buildlogic.configure.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            configureAndroidCompose(extensions.getByType<CommonExtension>())
        }
    }
}
