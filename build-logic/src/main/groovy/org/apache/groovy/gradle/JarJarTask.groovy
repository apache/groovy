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

import groovy.transform.AutoFinal

import javax.inject.Inject

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
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@AutoFinal @CacheableTask
class JarJarTask extends DefaultTask {

    @Internal
    String description = 'Repackages dependencies into a shaded jar'

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
    @Optional
    List<String> untouchedFiles = []

    @Input
    @Optional
    List<String> excludes = []

    @Input
    Map<String, String> patterns

    @Input
    @Optional
    Map<String, List<String>> excludesPerLibrary = [:]

    @Input
    @Optional
    Map<String, List<String>> includesPerLibrary = [:]

    @Input
    @Optional
    Map<String, String> includedResources = [:]

    @OutputFile
    final RegularFileProperty outputFile = project.objects.fileProperty()

    @Input
    boolean createManifest = true

    private final FileSystemOperations fs

    private List<Action<? super Manifest>> manifestTweaks = []

    @Inject
    JarJarTask(FileSystemOperations fileSystemOperations) {
        this.fs = fileSystemOperations
    }

    void withManifest(Action<? super Manifest> action) {
        manifestTweaks.add(action)
    }

    @Internal
    String getArchiveName() {
        outputFile.get().asFile.name
    }

    @TaskAction
    void generateDescriptor() {
        File outputFile = this.outputFile.get().asFile
        outputFile.parentFile.mkdirs()

        File tmpJar = new File(temporaryDir, "${archiveName}.${Integer.toHexString(UUID.randomUUID().hashCode())}.tmp")
        tmpJar.deleteOnExit()

        File manifestFile = new File(temporaryDir, 'MANIFEST.MF')
        manifestFile.deleteOnExit()

        // Use fixed date/timestamp for reproducible build
        String tstamp = Date.parse('yyyy-MM-dd HH:mm', '1980-02-01 00:00').getTime().toString()

        // Step 1: create a repackaged jar
        project.ant {
            taskdef name: 'jarjar', classname: 'com.eed3si9n.jarjar.JarJarTask', classpath: jarjarToolClasspath.asPath
            jarjar(jarfile: tmpJar, filesonly: true, modificationtime: tstamp) {
                zipfileset(src: from.get(), excludes: (untouchedFiles + excludes).join(','))
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

        fs.copy { spec ->
            spec.from(tmpJar)
            spec.into(outputFile.parentFile)
            spec.rename { this.archiveName }
        }

        // Step 2: update the archive with a class index and any untouched files
        project.ant.jar(destfile: outputFile, index: true, modificationtime: tstamp, update: true) {
            if (untouchedFiles) {
                zipfileset(src: from.get(), includes: untouchedFiles.join(','))
            }
        }

        // Step 3: generate an OSGi manifest referencing the repackaged classes
        if (createManifest) {
            def mf = project.rootProject.extensions.osgi.osgiManifest {
                symbolicName = project.name
                instruction 'Import-Package', '*;resolution:=optional'
                classesDir = tmpJar
            }
            manifestTweaks*.execute(mf)
            mf.writeTo(manifestFile)

            project.ant.zip(destfile: outputFile, modificationtime: tstamp, update: true) {
                zipfileset(dir: manifestFile.parent, includes: manifestFile.name, prefix: 'META-INF')
            }
        }
    }

    private static String baseName(File file) {
        file.name.substring(0, file.name.lastIndexOf('-'))
    }
}
