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

import aQute.bnd.osgi.Analyzer
import groovy.transform.AutoFinal

import javax.inject.Inject

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
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
    final RegularFileProperty from

    @InputFiles
    @Classpath
    final ConfigurableFileCollection repackagedLibraries

    @InputFiles
    @Classpath
    final ConfigurableFileCollection jarjarToolClasspath

    /**
     * Classpath passed to the BND {@link Analyzer} for OSGi import resolution.
     * Typically the project's {@code runtimeClasspath}.
     */
    @Classpath
    final ConfigurableFileCollection bndClasspath

    @Internal
    final String projectName

    @Input
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
    final RegularFileProperty outputFile

    @Input
    boolean createManifest = true

    /**
     * BND instructions used when generating the OSGi manifest.
     * Entries override the default analyzer properties unless explicitly appended
     * through {@link #appendBndInstruction(String, String)}.
     */
    @Input
    Map<String, String> bndInstructions = [:]

    private final FileSystemOperations fs

    @Inject
    JarJarTask(ObjectFactory objects, FileSystemOperations fileSystemOperations) {
        this.fs = fileSystemOperations
        this.from = objects.fileProperty()
        this.repackagedLibraries = objects.fileCollection()
        this.jarjarToolClasspath = objects.fileCollection()
        this.bndClasspath = objects.fileCollection()
        this.outputFile = objects.fileProperty()
        this.projectName = project.name
    }

    /**
     * Sets a BND manifest instruction, replacing any previous value for {@code key}.
     */
    void bndInstruction(String key, String value) {
        setBndInstruction(key, value)
    }

    /**
     * Sets a BND manifest instruction, replacing any previous value for {@code key}.
     */
    void setBndInstruction(String key, String value) {
        bndInstructions[key] = value
    }

    /**
     * Appends a BND manifest instruction using a comma separator, matching BND's
     * syntax for multi-value headers such as {@code Require-Capability}.
     */
    void appendBndInstruction(String key, String value) {
        def existing = bndInstructions.get(key)
        bndInstructions[key] = existing != null ? "${existing},${value}" : value
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
        ant.with {
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
        ant.jar(destfile: outputFile, index: true, modificationtime: tstamp, update: true) {
            if (untouchedFiles) {
                zipfileset(src: from.get(), includes: untouchedFiles.join(','))
            }
        }

        // Step 3: generate an OSGi manifest referencing the repackaged classes
        if (createManifest) {
            def analyzer = new Analyzer()
            try {
                analyzer.setJar(tmpJar)
                bndClasspath.files.each { File f ->
                    if (f.exists()) {
                        analyzer.addClasspath(f)
                    }
                }
                // Defaults — all overridable by entries in bndInstructions
                analyzer.setProperty('Bundle-SymbolicName', projectName)
                analyzer.setProperty('Import-Package', '*;resolution:=optional')
                // Strip BND-generated housekeeping headers that must not appear in released jars
                analyzer.setProperty('-removeheaders',
                    'Bnd-LastModified,Tool,Created-By,Originally-Created-By,Ant-Version')
                // User-specified instructions (override defaults above)
                bndInstructions.each { k, v -> analyzer.setProperty(k, v) }

                def manifest = analyzer.calcManifest()
                manifestFile.withOutputStream { os -> manifest.write(os) }
            } finally {
                analyzer.close()
            }

            ant.zip(destfile: outputFile, modificationtime: tstamp, update: true) {
                zipfileset(dir: manifestFile.parent, includes: manifestFile.name, prefix: 'META-INF')
            }
        }
    }

    private static String baseName(File file) {
        file.name.substring(0, file.name.lastIndexOf('-'))
    }
}
