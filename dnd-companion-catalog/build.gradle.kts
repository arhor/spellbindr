plugins {
    `java-library`
}

group = "io.github.arhor.dnd.companion"
version = "0.1.0-SNAPSHOT"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

repositories { mavenCentral() }

dependencies {
    api("io.github.arhor.dnd.companion:dnd-companion-schema:0.1.0-SNAPSHOT")
    implementation("com.google.protobuf:protobuf-java-util:4.32.1")
    implementation("com.google.code.gson:gson:2.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")
}

val generatedResources = layout.buildDirectory.dir("generated/catalog-resources")

val generateCatalogSnapshot by tasks.registering(JavaExec::class) {
    dependsOn(tasks.named("compileJava"))
    classpath = files(sourceSets.main.get().output.classesDirs, configurations.runtimeClasspath)
    mainClass = "io.github.arhor.dnd.companion.catalog.CatalogGenerator"
    args(
        rootDir.resolve("../dnd-companion-client-android/app/src/main/assets/data").canonicalPath,
        generatedResources.get().file("catalog/catalog.pb").asFile.absolutePath,
    )
    outputs.file(generatedResources.map { it.file("catalog/catalog.pb") })
}

sourceSets.main { resources.srcDir(generatedResources) }
tasks.processResources { dependsOn(generateCatalogSnapshot) }
tasks.test { useJUnitPlatform() }
