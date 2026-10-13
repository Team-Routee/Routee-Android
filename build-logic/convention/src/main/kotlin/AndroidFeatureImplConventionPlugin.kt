import com.routee.android.buildlogic.extension.libs
import com.routee.android.buildlogic.extension.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `:feature:x:impl` 화면 구현 모듈 설정.
 */
class AndroidFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("routee-android-library-compose"))
            pluginManager.apply(libs.pluginId("routee-hilt"))

            dependencies {
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:data"))
                "implementation"(project(":core:navigation"))
                "implementation"(project(":core:ui"))

                "implementation"(libs.findBundle("feature-lifecycle").get())
                "implementation"(libs.findBundle("navigation3").get())
            }
        }
    }
}
