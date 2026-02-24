plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-library")
}

dependencies {
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(project(":core:domain"))
}

kotlin {
    jvmToolchain(17)
}
