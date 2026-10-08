import com.routee.android.buildlogic.extension.libs
import com.routee.android.buildlogic.extension.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("routee-android-library"))
            pluginManager.apply(libs.pluginId("routee-android-compose"))
            pluginManager.apply(libs.pluginId("routee-hilt"))

            dependencies {
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:data"))

                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                "implementation"(libs.findLibrary("androidx-hilt-lifecycle-viewmodel-compose").get())
            }
        }
    }
}
