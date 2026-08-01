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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.transform.AutoFinal
import groovy.transform.CompileDynamic

import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.java.archives.Manifest
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Repackages selected dependency classes into a shaded jar using the
 * {@code com.gradleup.shadow} relocation engine, then optionally rewrites
 * the OSGi manifest and Jar index so published artefacts stay compatible
 * with historical output (including {@code groovyjarjar*} package prefixes).
 *
 * <p>Registered under the task name {@code repackageJar}. The shaded artefact
 * is {@link #getArchiveFile()}.
 */
@AutoFinal
@CacheableTask
@CompileDynamic
abstract class RepackageJarTask extends ShadowJar {

    /**
     * Base jar to relocate (typically the module's {@code jar} task output,
     * or a previously shaded jar for chained repackaging such as grooid/javax).
     */
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty inputJar = project.objects.fileProperty()

    /**
     * Dependency jars whose classes are merged and relocated into the output.
     */
    @InputFiles
    @Classpath
    final ConfigurableFileCollection repackagedLibraries = project.objects.fileCollection()

    @Input
    @Optional
    List<String> untouchedFiles = []

    /**
     * Extra paths excluded from the input jar before relocation
     * (not to be confused with ShadowJar's own {@code excludes} CopySpec).
     */
    @Input
    @Optional
    List<String> sourceExcludes = []

    /**
     * Relocation patterns in compact form
     * ({@code 'org.antlr.**': 'groovyjarjarantlr4.@1'}), converted to Shadow
     * prefix relocations. Destination package prefixes such as
     * {@code groovyjarjar*} are part of the published artefact surface and
     * must not change.
     */
    @Input
    Map<String, String> patterns = [:]

    @Input
    @Optional
    Map<String, List<String>> excludesPerLibrary = [:]

    @Input
    @Optional
    Map<String, List<String>> includesPerLibrary = [:]

    @Input
    @Optional
    Map<String, String> includedResources = [:]

    /**
     * When true, rewrite the jar with an OSGi manifest (bnd via the root osgi extension).
     */
    @Input
    boolean generateOsgiManifest = true

    private final Object osgiExtension = project.rootProject.extensions.findByName('osgi')

    private final String projectName = project.name

    private final List<Action<? super Manifest>> manifestTweaks = []

    RepackageJarTask() {
        description = 'Repackages dependencies into a shaded jar (com.gradleup.shadow)'
        // Only shade the jars we explicitly feed in; never pull runtimeClasspath wholesale.
        configurations.set([])
        // First-wins for overlapping paths (e.g. antlr4-runtime vs antlr4-annotations).
        duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
        failOnDuplicateEntries.set(false)
        preserveFileTimestamps = false
        reproducibleFileOrder = true
        // Published shaded artefact has no classifier/appendix (unlike the raw jar).
        archiveClassifier.convention('')
        archiveAppendix.convention('')

        // Default dep hygiene also applied by Shadow's own registerShadowJarCommon.
        exclude(
                'META-INF/INDEX.LIST',
                'META-INF/*.SF',
                'META-INF/*.DSA',
                'META-INF/*.RSA',
                'META-INF/versions/**/module-info.class',
                'module-info.class',
                '**/module-info.class'
        )

        // Register sources once with lazy evaluation so re-runs don't double-add entries.

        // Main jar contents (untouched files are re-added without relocation in postProcess).
        from({
            FileTree tree = project.zipTree(inputJar.get().asFile)
            List<String> skip = (untouchedFiles + sourceExcludes).findAll { it }
            if (skip) {
                return tree.matching { spec -> skip.each { spec.exclude(it) } }
            }
            tree
        })

        // Extra resources staged into temporaryDir/included-resources with final archive paths.
        from({
            File resourceStage = new File(temporaryDir, 'included-resources')
            resourceStage.exists() ? resourceStage : []
        })

        // Libraries with per-artifact includes/excludes.
        from({
            List<String> defaultLibExcludes = excludesPerLibrary['*'] ?: [
                    'META-INF/maven/**',
                    'META-INF/*',
                    'META-INF/services/javax.annotation.processing.Processor',
                    '**/module-info.class'
            ]
            List<FileTree> trees = []
            repackagedLibraries.files.each { File library ->
                // Artifact id without version suffix (e.g. asm-util-9.x.jar -> asm-util).
                // Inline to avoid clashing with ArchiveTask.baseName in the CopySpec delegate.
                String libraryName = library.name.substring(0, library.name.lastIndexOf('-'))
                List<String> includes = includesPerLibrary[libraryName]
                List<String> libExcludes = excludesPerLibrary[libraryName]
                FileTree tree = project.zipTree(library)
                if (includes) {
                    trees << tree.matching { spec -> includes.each { spec.include(it) } }
                } else if (libExcludes) {
                    trees << tree.matching { spec -> libExcludes.each { spec.exclude(it) } }
                } else {
                    trees << tree.matching { spec -> defaultLibExcludes.each { spec.exclude(it) } }
                }
            }
            trees
        })
    }

    void withManifest(Action<? super Manifest> action) {
        manifestTweaks.add(action)
    }

    @Internal
    String getArchiveName() {
        archiveFile.get().asFile.name
    }

    @TaskAction
    @Override
    void copy() {
        // Stage included resources before the copy action resolves its file trees.
        File resourceStage = new File(temporaryDir, 'included-resources')
        if (resourceStage.exists()) {
            project.delete(resourceStage)
        }
        resourceStage.mkdirs()
        includedResources.each { String resource, String path ->
            File src = project.file(resource)
            File dest = new File(resourceStage, path)
            dest.parentFile.mkdirs()
            dest.bytes = src.bytes
        }

        // Relocators must be (re)applied here so pattern assignments done after task
        // registration are honoured, without accumulating across re-runs.
        relocators.empty()
        for (Map.Entry<String, String> e : patterns.entrySet()) {
            String pattern = e.key
            String prefix = toRelocationPrefix(pattern)
            String destination = toRelocationDestination(e.value)
            // Shadow matches package prefixes with startsWith, so a rule for
            // java.beans.** also rewrites the bare string "java.beans". jarjar's
            // ".**" wildcard required a child segment and left bare package names
            // alone. That matters for Java9.JAVA8_PACKAGES(): those entries are
            // real JDK package names looked up via ModuleFinder.ofSystem() — if
            // rewritten to groovyjarjaropenbeans the lookup no-ops. Type/descriptor
            // remapping is unaffected (skipStringConstants only gates LDC strings).
            // See GROOVY-12199 / Bucket 4a.
            if (shouldSkipStringConstants(prefix)) {
                relocate(prefix, destination) { it.skipStringConstants = true }
            } else {
                relocate(prefix, destination)
            }
        }

        super.copy()
        postProcess()
    }

    /**
     * JDK packages that may appear as bare package-name string constants (not
     * Class.forName targets) and must stay as real platform names after shading.
     */
    private static boolean shouldSkipStringConstants(String packagePrefix) {
        return packagePrefix == 'java.beans' || packagePrefix.startsWith('java.beans.')
    }

    private void postProcess() {
        File out = archiveFile.get().asFile
        // Fixed timestamp for reproducible builds (matches historical packaging).
        String tstamp = Date.parse('yyyy-MM-dd HH:mm', '1980-02-01 00:00').time.toString()

        // Re-add untouched entries from the input jar without relocation and generate the
        // Jar index (META-INF/INDEX.LIST). Both happen in a single update-in-place pass:
        // each ant update renames the artefact aside and rewrites it, so a second pass is
        // pure overhead — and on Windows another chance to trip over a stray file handle.
        ant.jar(destfile: out, index: true, modificationtime: tstamp, update: true) {
            if (untouchedFiles) {
                zipfileset(src: inputJar.get().asFile, includes: untouchedFiles.join(','))
            }
        }

        if (generateOsgiManifest && osgiExtension != null) {
            File manifestFile = new File(temporaryDir, 'MANIFEST.MF')
            // bnd (via the legacy osgi plugin) never closes its Analyzer, so it keeps an open
            // handle on every jar it reads. On Windows that blocks the rename-to-temp which
            // ant performs for update:true, so analyse a throwaway copy rather than the
            // artefact we are about to update. The name must be unique per run: a fixed one
            // would still be locked by the previous run's leak. See GROOVY-12199.
            File analysisJar = new File(temporaryDir, "${out.name}.${Integer.toHexString(UUID.randomUUID().hashCode())}.tmp")
            analysisJar.bytes = out.bytes
            analysisJar.deleteOnExit()
            String bundleSymbolicName = projectName
            def mf = osgiExtension.osgiManifest {
                symbolicName = bundleSymbolicName
                instruction 'Import-Package', '*;resolution:=optional'
                classesDir = analysisJar
            }
            for (Action<? super Manifest> tweak : manifestTweaks) {
                tweak.execute(mf)
            }
            mf.writeTo(manifestFile)

            ant.zip(destfile: out, modificationtime: tstamp, update: true) {
                zipfileset(dir: manifestFile.parent, includes: manifestFile.name, prefix: 'META-INF')
            }
        }
    }

    /**
     * {@code org.antlr.**} -> {@code org.antlr}; {@code picocli.**} -> {@code picocli}.
     */
    private static String toRelocationPrefix(String pattern) {
        String p = pattern
        if (p.endsWith('.**')) {
            p = p.substring(0, p.length() - 3)
        } else if (p.endsWith('.*')) {
            p = p.substring(0, p.length() - 2)
        }
        return p
    }

    /**
     * {@code groovyjarjarantlr4.@1} -> {@code groovyjarjarantlr4}.
     */
    private static String toRelocationDestination(String result) {
        String r = result
        if (r.endsWith('.@1')) {
            r = r.substring(0, r.length() - 3)
        } else if (r.contains('@1')) {
            r = r.replace('.@1', '').replace('@1', '')
        }
        return r
    }

}
