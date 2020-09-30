package org.apache.groovy.gradle

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CacheableTask
@CompileStatic
class DocGDK extends DefaultTask {
    @InputFiles
    @Classpath
    final ConfigurableFileCollection classpath = project.objects.fileCollection()

    @OutputDirectory
    final DirectoryProperty outputDirectory = project.objects.directoryProperty()
            .convention(project.layout.buildDirectory.dir("html/groovy-jdk"))


    @Input
    final ListProperty<String> classes = project.objects.listProperty(String)

    @TaskAction
    @CompileDynamic
    void generateDocs() {
        try {
            project.ant {
                java(classname: 'org.apache.groovy.docgenerator.DocGenerator',
                        fork: 'true',
                        failonerror: 'true',
                        classpath: classpath.asPath,
                        errorproperty: 'edr',
                        outputproperty: 'odr') {
                    arg(value: '-title')
                    arg(value: 'Groovy JDK enhancements')
                    arg(value: '-link')
                    arg(value: 'groovy,org.codehaus.groovy=https://docs.groovy-lang.org/latest/html/gapi/')
                    arg(value: '-link')
                    arg(value: 'java,org.xml,javax,org.w3c=https://docs.oracle.com/javase/8/docs/api/')
                    classes.get().each {
                        arg(value: it)
                    }
                }
            }
        } finally {
            if (ant.properties.odr) {
                logger.info 'Out: ' + ant.properties.odr
            }
            if (ant.properties.edr) {
                logger.error 'Err: ' + ant.properties.edr
            }
        }
        project.copy {
            into outputDirectory.get()
            from project.project(':groovy-docgenerator').file('src/main/resources/org/apache/groovy/docgenerator/groovy.ico')
            from project.project(':groovy-docgenerator').file('src/main/resources/org/apache/groovy/docgenerator/stylesheet.css')
        }
    }
}
