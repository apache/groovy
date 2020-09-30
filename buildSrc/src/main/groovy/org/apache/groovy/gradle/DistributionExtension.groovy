package org.apache.groovy.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty

import javax.inject.Inject

@CompileStatic
class DistributionExtension {
    final ListProperty<String> docgeneratorClasses
    final Project project

    CopySpec distSpec
    CopySpec srcSpec
    CopySpec docSpec

    @Inject
    DistributionExtension(ObjectFactory factory, Project project) {
        this.docgeneratorClasses = factory.listProperty(String).convention([])
        this.project = project
    }

    DistributionExtension docs(String p, String... classNames) {
        project.tasks.named('docGDK').configure { DocGDK docgen ->
            docgeneratorClasses.set(docgeneratorClasses.get() +
                    classNames.collect { className ->
                        def src = project.project(p).layout.projectDirectory.file(
                                "src/main/java/${className.replace('.', '/')}.java"
                        ).asFile
                        docgen.inputs.file(src)
                        src.absolutePath
                    }
            )
        }
        this
    }
}
