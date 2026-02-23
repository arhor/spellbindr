plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.github.arhor.spellbindr.core.common.android"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlin.reflect)
}
