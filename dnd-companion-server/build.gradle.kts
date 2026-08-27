plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.quarkus)
}

group = "io.github.arhor.dnd.companion"
version = "0.1.0-SNAPSHOT"

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

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.arhor.dnd.companion:dnd-companion-schema:0.1.0-SNAPSHOT")
    implementation("io.github.arhor.dnd.companion:dnd-companion-catalog:0.1.0-SNAPSHOT")
    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(libs.protobuf.kotlin)
    implementation(libs.quarkus.grpc)
    implementation(libs.quarkus.kotlin)
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(libs.quarkus.junit)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
