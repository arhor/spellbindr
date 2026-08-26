plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.quarkus)
}

group = "io.github.arhor.dnd.companion"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(libs.protobuf.kotlin)
    implementation(libs.quarkus.grpc)
    implementation(libs.quarkus.kotlin)
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(libs.quarkus.junit)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        javaParameters = true
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
