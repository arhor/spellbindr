plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.android.hilt)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.github.arhor.spellbindr.data.preferences"
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
    ksp(libs.hilt.android.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)

    implementation(project(":core:common-android"))
    implementation(project(":core:domain"))

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}
