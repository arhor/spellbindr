plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(project(":core:testing"))
}

kotlin {
    jvmToolchain(17)
}
