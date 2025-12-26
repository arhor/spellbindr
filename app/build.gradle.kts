import com.github.arhor.spellbindr.build.StripPreviewClasses
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.hilt)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
    jacoco
}

android {
    namespace = "com.github.arhor.spellbindr"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.github.arhor.spellbindr"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "com.github.arhor.spellbindr.HiltApplicationTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs.addAll("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

tasks.withType<Test>().configureEach {
    extensions.configure(JacocoTaskExtension::class) {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

// Compose previews are stripped out via StripPreviewClasses; these patterns clean
// up any generated helpers that remain after bytecode filtering.
val jacocoFileFilter = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "**/*\$NoOp*.*",
    "**/*\$inlined\$*.*",
    "**/*ComposableSingletons*.*",
    "**/*Preview*.*",
    "**/ui/theme/**",
)

androidComponents.onVariants(androidComponents.selector().withBuildType("debug")) { variant ->
    val variantNameCapitalized = variant.name.replaceFirstChar { char -> char.titlecase() }
    val unitTestTaskName = "test${variantNameCapitalized}UnitTest"

    val filteredClassesDir = layout.buildDirectory.dir("jacoco/filteredClasses/${variant.name}")
    val stripPreviewClasses = tasks.register("strip${variantNameCapitalized}ComposePreviews", StripPreviewClasses::class) {
        kotlinClassesDir.set(layout.buildDirectory.dir("tmp/kotlin-classes/${variant.name}").map { dir ->
            dir.asFile.apply { mkdirs() }
            dir
        })
        javaClassesDir.set(layout.buildDirectory.dir("intermediates/javac/${variant.name}/classes").map { dir ->
            dir.asFile.apply { mkdirs() }
            dir
        })
        outputDir.set(filteredClassesDir)
    }
    stripPreviewClasses.configure {
        dependsOn(tasks.named("compile${variantNameCapitalized}Kotlin"))
        dependsOn(tasks.named("compile${variantNameCapitalized}JavaWithJavac"))
    }

    val jacocoReport = tasks.register("jacoco${variantNameCapitalized}Report", JacocoReport::class) {
        group = "verification"
        description = "Generates Jacoco coverage report for the ${variant.name} build."
        notCompatibleWithConfigurationCache("Jacoco report setup relies on dynamic class filtering.")

        dependsOn(unitTestTaskName, stripPreviewClasses)

        reports {
            html.required = true
            xml.required = true
        }

        val filteredClassTree = stripPreviewClasses.flatMap { task ->
            task.outputDir.map { dir ->
                fileTree(dir) { exclude(jacocoFileFilter) }
            }
        }

        classDirectories.setFrom(filteredClassTree)
        sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
        executionData.setFrom(files(layout.buildDirectory.file("jacoco/${unitTestTaskName}.exec")))
    }

    tasks.named("check").configure {
        dependsOn(jacocoReport)
    }
}

dependencies {
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.room.compiler)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.hilt.android)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.archunit.junit4)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    kspAndroidTest(libs.hilt.android.compiler)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.hilt.android.testing)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
