package org.apache.groovy.gradle

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CompileStatic
@CacheableTask
class DgmConverter extends DefaultTask {

    @OutputDirectory
    final DirectoryProperty outputDirectory

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    final ConfigurableFileTree sources

    @InputFiles
    @Classpath
    final ConfigurableFileCollection classpath

    DgmConverter() {
        description = 'Generates DGM info file required for faster startup.'
        classpath = project.objects.fileCollection()
        sources = project.objects.fileTree()
        outputDirectory = project.objects.directoryProperty().convention(
                project.layout.buildDirectory.dir("dgm")
        )
    }

    @TaskAction
    @CompileDynamic
    void generateDgmInfo() {
        outputDirectory.dir("META-INF").get().asFile.mkdirs()
        // we use ant.java because Gradle is a bit "too smart" with JavaExec
        // as it will invalidate the task if classpath changes, which will
        // happen once Groovy files are compiled
        project.ant.java(classname: 'org.codehaus.groovy.tools.DgmConverter', classpath: classpath.asPath) {
            arg(value: '--info')
            arg(value: "${outputDirectory.get().asFile.absolutePath}")
        }
    }
}
