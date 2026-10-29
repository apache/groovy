/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.gradle

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

import javax.inject.Inject

/**
 * Generates AsciiDoc reference documentation for a {@code FactoryBuilderSupport}
 * builder by introspecting a live instance of it from the project being built.
 *
 * The generator script is bundled as a resource in the build-logic jar. At
 * execution time it is extracted to a temporary file and run via
 * {@code groovy.ui.GroovyMain} in a forked JVM whose classpath contains the
 * module's classes (plus the core Groovy jar they depend on). The script
 * receives the output directory as its single argument.
 */
@CacheableTask
@CompileStatic
class BuilderDocTask extends DefaultTask {

    private final ExecOperations execOperations

    /** Classpath resource of the generator script, e.g. {@code org/apache/groovy/gradle/GenerateFooDocs.groovy}. */
    @Input
    final Property<String> scriptResource = project.objects.property(String)

    /** JVM arguments for the forked generator, such as headless-mode system properties. */
    @Input
    final ListProperty<String> jvmArgs = project.objects.listProperty(String)

    @InputFiles
    @Classpath
    final ConfigurableFileCollection classpath = project.objects.fileCollection()

    @OutputDirectory
    final DirectoryProperty outputDirectory = project.objects.directoryProperty()

    @Inject
    BuilderDocTask(ExecOperations execOperations) {
        this.execOperations = execOperations
        group = 'documentation'
        jvmArgs.convention(['-Djava.awt.headless=true'])
    }

    @TaskAction
    @CompileDynamic
    void generate() {
        outputDirectory.get().asFile.mkdirs()

        // Extract the generator script from build-logic resources into Gradle's task temp dir
        String resource = scriptResource.get()
        File scriptFile = new File(temporaryDir, resource.substring(resource.lastIndexOf('/') + 1))
        getClass().classLoader.getResourceAsStream(resource).withStream { input ->
            scriptFile.bytes = input.bytes
        }

        execOperations.javaexec {
            it.mainClass.set('groovy.ui.GroovyMain')
            it.classpath = this.classpath
            it.jvmArgs(this.jvmArgs.get())
            it.args(scriptFile.absolutePath, outputDirectory.asFile.get().absolutePath)
        }
    }
}
