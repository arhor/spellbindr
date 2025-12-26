package com.github.arhor.spellbindr.build

import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Transforms class files to drop Compose previews from coverage.")
abstract class StripPreviewClasses @Inject constructor() : DefaultTask() {

    @get:Optional
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val kotlinClassesDir: DirectoryProperty

    @get:Optional
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val javaClassesDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun filterPreviewComposables() {
        val destinationDir = outputDir.get().asFile
        destinationDir.deleteRecursively()
        destinationDir.mkdirs()

        listOfNotNull(kotlinClassesDir.orNull?.asFile, javaClassesDir.orNull?.asFile)
            .filter(File::exists)
            .forEach { classesDir ->
                classesDir.walkTopDown()
                    .filter(File::isFile)
                    .forEach { file ->
                        val target = destinationDir.resolve(file.relativeTo(classesDir).path)
                        target.parentFile.mkdirs()

                        if (file.extension == "class") {
                            target.writeBytes(file.readBytes().stripPreviewAnnotatedMethods())
                        } else {
                            file.copyTo(target, overwrite = true)
                        }
                    }
            }
    }

    private fun ByteArray.stripPreviewAnnotatedMethods(): ByteArray {
        val classNode = ClassNode()
        ClassReader(this).accept(classNode, 0)

        val previewMethodNames = classNode.methods
            .filter { method -> method.isComposablePreview() }
            .map { method -> method.name.substringBefore('$') }
            .toSet()
        val removedAnyPreviews = classNode.methods.removeIf { method ->
            previewMethodNames.any { previewName ->
                method.name == previewName || method.name.startsWith("$previewName$")
            }
        }

        if (!removedAnyPreviews) return this

        val classWriter = ClassWriter(0)
        classNode.accept(classWriter)
        return classWriter.toByteArray()
    }

    private fun MethodNode.isComposablePreview(): Boolean {
        if (!name.contains("Preview")) return false

        val annotationDescriptors = buildList {
            visibleAnnotations?.let(::addAll)
            invisibleAnnotations?.let(::addAll)
        }.map(AnnotationNode::desc)

        return annotationDescriptors.any { descriptor ->
            descriptor == COMPOSABLE_ANNOTATION_DESCRIPTOR || descriptor == PREVIEW_ANNOTATION_DESCRIPTOR
        }
    }

    private companion object {
        private const val COMPOSABLE_ANNOTATION_DESCRIPTOR = "Landroidx/compose/runtime/Composable;"
        private const val PREVIEW_ANNOTATION_DESCRIPTOR = "Landroidx/compose/ui/tooling/preview/Preview;"
    }
}
