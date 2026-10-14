import com.routee.android.buildlogic.extension.libs
import com.routee.android.buildlogic.extension.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `:feature:x:api` 화면 키 모듈 설정
 */
class AndroidFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("routee-android-library"))
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                "implementation"(libs.findBundle("navigation3-key").get())
            }
        }
    }
}
