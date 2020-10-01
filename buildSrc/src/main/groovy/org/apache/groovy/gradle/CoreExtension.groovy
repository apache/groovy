package org.apache.groovy.gradle

import groovy.transform.CompileStatic
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.SourceSetContainer

import javax.inject.Inject

@CompileStatic
class CoreExtension {
    final ConfigurableFileCollection classesToBridge
    final SourceSetContainer sourceSets
    final List<String> excludedFromJavadocs = []

    @Inject
    CoreExtension(ObjectFactory objects, SourceSetContainer sourceSets) {
        this.classesToBridge = objects.fileCollection()
        this.sourceSets = sourceSets
    }

    void bridgedClasses(String... classes) {
        classes.each {
            def baseDir = sourceSets.getByName("main").java.outputDir
            classesToBridge.from(new File(baseDir, "/" + it.replace((char) '.', (char) '/') + ".class"))
        }
    }

    void excludeFromJavadoc(String... items) {
        Collections.addAll(excludedFromJavadocs, items)
    }
}
