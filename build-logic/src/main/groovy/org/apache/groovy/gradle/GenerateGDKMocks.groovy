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
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

import javax.inject.Inject

/**
 * Runs {@code MockSourceGenerator} against the supplied DGM source files,
 * emitting one Java mock per receiver type plus a manifest file used by the
 * downstream rename pass. Upstream half of the pipeline that replaced the old
 * monolithic {@code DocGenerator} step.
 */
@CacheableTask
@CompileStatic
class GenerateGDKMocks extends DefaultTask {

    private final ExecOperations execOperations

    @InputFiles
    @Classpath
    final ConfigurableFileCollection classpath = project.objects.fileCollection()

    @Input
    final ListProperty<String> classes = project.objects.listProperty(String)

    @OutputDirectory
    final DirectoryProperty outputDirectory = project.objects.directoryProperty()
            .convention(project.layout.buildDirectory.dir('tmp/groovy-jdk-mocks'))

    @Inject
    GenerateGDKMocks(ExecOperations execOperations) {
        this.execOperations = execOperations
    }

    @TaskAction
    @CompileDynamic
    void generate() {
        execOperations.javaexec {
            it.mainClass.set('org.apache.groovy.docgenerator.MockSourceGenerator')
            it.classpath = this.classpath
            it.args(['-o', outputDirectory.get().asFile.absolutePath] + classes.get())
        }
    }
}
