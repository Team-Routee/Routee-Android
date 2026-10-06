import com.routee.android.buildlogic.extension.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("routee.android.library")
            pluginManager.apply("routee.android.compose")
            pluginManager.apply("routee.hilt")

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
