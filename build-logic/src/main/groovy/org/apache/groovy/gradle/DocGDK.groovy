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
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

import javax.inject.Inject

@CacheableTask
@CompileStatic
class DocGDK extends DefaultTask {

    private final ExecOperations execOperations
    private final FileSystemOperations fs

    @InputFiles
    @Classpath
    final ConfigurableFileCollection classpath = project.objects.fileCollection()

    @OutputDirectory
    final DirectoryProperty outputDirectory = project.objects.directoryProperty()
            .convention(project.layout.buildDirectory.dir("html/groovy-jdk"))


    @Input
    final ListProperty<String> classes = project.objects.listProperty(String)

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    final ConfigurableFileCollection resources = project.objects.fileCollection()

    @Inject
    DocGDK(ExecOperations execOperations, FileSystemOperations fileSystemOperations) {
        this.execOperations = execOperations
        this.fs = fileSystemOperations
    }

    @TaskAction
    @CompileDynamic
    void generateDocs() {
        execOperations.javaexec {
            it.mainClass.set('org.apache.groovy.docgenerator.DocGenerator')
            it.classpath = this.classpath
            it.args(
                    ['-title',
                     'Groovy JDK enhancements',
                     '-link',
                     'groovy,org.codehaus.groovy,org.apache.groovy=https://docs.groovy-lang.org/latest/html/gapi/',
                     '-link',
                     'java,org.xml,javax,org.w3c=https://docs.oracle.com/javase/8/docs/api/'] + classes.get()
            )
        }
        fs.copy {
            into outputDirectory
            from resources
        }
    }
}
