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

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.java.archives.Manifest
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

@CacheableTask
class JarJarTask extends DefaultTask {
    private final static String JARJAR_CLASS_NAME = 'org.pantsbuild.jarjar.JarJarTask'

    @Internal
    String description = "Repackages dependencies into a shaded jar"

    private List<Action<? super Manifest>> manifestTweaks = []

    private final FileSystemOperations fs

    @InputFile
    @Classpath
    final RegularFileProperty from = project.objects.fileProperty()

    @InputFiles
    @Classpath
    final ConfigurableFileCollection repackagedLibraries = project.objects.fileCollection()

    @InputFiles
    @Classpath
    final ConfigurableFileCollection jarjarToolClasspath = project.objects.fileCollection()

    @InputFiles
    @Classpath
    @org.gradle.api.tasks.Optional
    List<String> untouchedFiles = []

    @Input
    @org.gradle.api.tasks.Optional
    List<String> excludes = []

    @Input
    Map<String, String> patterns

    @Input
    @org.gradle.api.tasks.Optional
    Map<String, List<String>> excludesPerLibrary = [:]

    @Input
    @org.gradle.api.tasks.Optional
    Map<String, List<String>> includesPerLibrary = [:]

    @Input
    @org.gradle.api.tasks.Optional
    Map<String, String> includedResources = [:]

    @OutputFile
    final RegularFileProperty outputFile = project.objects.fileProperty()

    @Input
    boolean createManifest = true

    @Inject
    JarJarTask(FileSystemOperations fileSystemOperations) {
        this.fs = fileSystemOperations
    }

    void withManifest(Action<? super Manifest> action) {
        manifestTweaks << action
    }

    @Internal
    String getArchiveName() {
        outputFile.get().asFile.name
    }

    @TaskAction
    void generateDescriptor() {
        def originalJar = from.get()
        def outputFile = this.outputFile.get().asFile
        def tmpJar = new File(temporaryDir, "${outputFile.name}.${Integer.toHexString(UUID.randomUUID().hashCode())}.tmp")
        def manifestFile = new File(temporaryDir, 'MANIFEST.MF')
        // First step is to create a repackaged jar
        outputFile.parentFile.mkdirs()
        try {
            project.ant {
                taskdef name: 'jarjar', classname: JARJAR_CLASS_NAME, classpath: jarjarToolClasspath.asPath
                jarjar(jarfile: tmpJar, filesonly: true) {
                    zipfileset(
                            src: originalJar,
                            excludes: (untouchedFiles + excludes).join(','))
                    includedResources.each { String resource, String path ->
                        String dir = resource.substring(0, resource.lastIndexOf('/') + 1)
                        String filename = resource.substring(resource.lastIndexOf('/') + 1)
                        zipfileset(dir: dir, includes: filename, fullpath: path)
                    }
                    repackagedLibraries.files.each { File library ->
                        def libraryName = JarJarTask.baseName(library)
                        def includes = includesPerLibrary[libraryName]
                        def excludes = excludesPerLibrary[libraryName]
                        if (includes) {
                            zipfileset(src: library, includes: includes.join(','))
                        } else if (excludes) {
                            zipfileset(src: library, excludes: excludes.join(','))
                        } else {
                            zipfileset(src: library, excludes: excludesPerLibrary['*'].join(','))
                        }
                    }
                    patterns.each { pattern, result ->
                        rule pattern: pattern, result: result
                    }
                }
            }

            if (createManifest) {
                // next step is to generate an OSGI manifest using the newly repackaged classes
                def mf = project.rootProject.convention.plugins.osgi.osgiManifest {
                    symbolicName = project.name
                    instruction 'Import-Package', '*;resolution:=optional'
                    classesDir = tmpJar
                }

                manifestTweaks.each {
                    it.execute(mf)
                }

                // then we need to generate the manifest file
                mf.writeTo(manifestFile)

            } else {
                manifestFile << ''
            }

            // so that we can put it into the final jar
            fs.copy {
                it.from(tmpJar)
                it.into(outputFile.parentFile)
                it.rename { outputFile.name }
            }
            project.ant.jar(destfile: outputFile, update: true, index: true, manifest: manifestFile) {
                manifest {
                    // because we don't want to use JDK 1.8.0_91, we don't care and it will
                    // introduce cache misses
                    attribute(name: 'Created-By', value: 'Gradle')
                }
                if (untouchedFiles) {
                    zipfileset(
                            src: originalJar,
                            includes: untouchedFiles.join(','))
                }
            }
        } finally {
            fs.delete {
                it.delete(manifestFile)
                it.delete(tmpJar)
            }
        }
    }

    @CompileStatic
    private static String baseName(File file) {
        file.name.substring(0, file.name.lastIndexOf('-'))
    }

}