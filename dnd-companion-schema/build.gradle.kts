plugins {
    `java-library`
    id("com.google.protobuf") version "0.9.6"
}

group = "io.github.arhor.dnd.companion"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.protobuf:protobuf-java:4.32.1")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.32.1"
    }
}
