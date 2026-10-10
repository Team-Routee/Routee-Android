import com.routee.android.buildlogic.extension.libs
import com.routee.android.buildlogic.extension.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("routee-android-library-compose"))
            pluginManager.apply(libs.pluginId("routee-hilt"))

            dependencies {
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:data"))

                "implementation"(libs.findBundle("feature-lifecycle").get())
            }
        }
    }
}
