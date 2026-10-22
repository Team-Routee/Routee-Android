plugins {
    alias(libs.plugins.routee.android.library)
    alias(libs.plugins.routee.hilt)
    alias(libs.plugins.room)
}

android {
    namespace = "com.routee.android.core.database"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
