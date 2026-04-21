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

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.javadoc.Groovydoc

/**
 * Replaces Gradle's built-in {@code Groovydoc} task execution with a direct
 * {@code AntBuilder} invocation of Groovy's {@code org.codehaus.groovy.ant.Groovydoc}
 * Ant task. This exposes properties not available on the Gradle task type:
 * {@code javaVersion}, {@code showInternal}, {@code noIndex}, {@code noDeprecatedList},
 * {@code noHelp}, {@code syntaxHighlighter}, {@code theme}, and additional stylesheets.
 *
 * <p>The Gradle task is retained as the configuration surface (source, classpath,
 * titles, links, access, etc.). Only the execution action is swapped. Set
 * {@code groovydocAnt.useAntBuilder = false} before task realization to revert
 * to Gradle's native execution for the project.
 *
 * <p>For aggregator tasks whose source is a resolvable {@code Configuration}
 * of source directories (not a {@code SourceSet}), set
 * {@code task.ext.groovydocSourceDirs} to the {@code FileCollection} of
 * directories to pass as {@code sourcepath}.
 */
@CompileStatic
class GroovydocAntPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        GroovydocAntExtension extension = project.extensions.create(
                'groovydocAnt', GroovydocAntExtension)
        configureJavaVersionDefault(project, extension)
        configureGroovydocTasks(project, extension)
    }

    @CompileDynamic
    private static void configureJavaVersionDefault(Project project, GroovydocAntExtension extension) {
        def shared = project.extensions.findByName('sharedConfiguration')
        if (shared != null && shared.hasProperty('targetJavaVersion')) {
            extension.javaVersion.convention(
                    shared.targetJavaVersion.map { "JAVA_${it}" as String }
            )
        }
    }

    @CompileDynamic
    private static void configureGroovydocTasks(Project project, GroovydocAntExtension extension) {
        project.tasks.withType(Groovydoc).configureEach { gdoc ->
            gdoc.inputs.property('antJavaVersion', extension.javaVersion.orElse(''))
            gdoc.inputs.property('antShowInternal', extension.showInternal)
            gdoc.inputs.property('antNoIndex', extension.noIndex)
            gdoc.inputs.property('antNoDeprecatedList', extension.noDeprecatedList)
            gdoc.inputs.property('antNoHelp', extension.noHelp)
            gdoc.inputs.property('antSyntaxHighlighter', extension.syntaxHighlighter)
            gdoc.inputs.property('antTheme', extension.theme)
            gdoc.inputs.property('antPreLanguage', extension.preLanguage)
            gdoc.inputs.files(extension.additionalStylesheets)
                    .withPropertyName('antAdditionalStylesheets')
                    .optional(true)

            if (!extension.useAntBuilder.get()) {
                return
            }

            gdoc.actions.clear()
            gdoc.doLast {
                executeGroovydoc(gdoc, extension, project)
            }
        }
    }

    @CompileDynamic
    private static void executeGroovydoc(Groovydoc gdoc, GroovydocAntExtension extension, Project project) {
        File destDir = resolveFile(gdoc.destinationDir)
        destDir.mkdirs()

        List<File> sourceDirs = resolveSourceDirectories(gdoc, project)
        if (sourceDirs.isEmpty()) {
            throw new GradleException(
                    "Groovydoc task '${gdoc.name}': no source directories found. " +
                    "Every published module must produce a groovydoc jar for Maven Central."
            )
        }

        FileCollection classpath = gdoc.groovyClasspath
        if (!classpath || classpath.empty) {
            throw new GradleException(
                    "Groovydoc task '${gdoc.name}': groovyClasspath is empty."
            )
        }

        project.ant.taskdef(
                name: 'groovydoc',
                classname: 'org.codehaus.groovy.ant.Groovydoc',
                classpath: classpath.asPath
        )

        Map<String, String> antArgs = [
                destdir: destDir.absolutePath,
                sourcepath: sourceDirs.collect { it.absolutePath }.join(File.pathSeparator),
                packagenames: '**.*',
                windowtitle: gdoc.windowTitle ?: '',
                doctitle: gdoc.docTitle ?: '',
                header: gdoc.header ?: '',
                footer: gdoc.footer ?: '',
                access: resolveAccess(gdoc),
                author: String.valueOf(unwrap(gdoc.includeAuthor, false)),
                noTimestamp: String.valueOf(unwrap(gdoc.noTimestamp, false)),
                noVersionStamp: String.valueOf(unwrap(gdoc.noVersionStamp, false)),
                processScripts: String.valueOf(unwrap(gdoc.processScripts, true)),
                includeMainForScripts: String.valueOf(unwrap(gdoc.includeMainForScripts, true)),
                showInternal: String.valueOf(extension.showInternal.get()),
                noIndex: String.valueOf(extension.noIndex.get()),
                noDeprecatedList: String.valueOf(extension.noDeprecatedList.get()),
                noHelp: String.valueOf(extension.noHelp.get()),
                syntaxHighlighter: extension.syntaxHighlighter.get(),
                theme: extension.theme.get()
        ]

        String jv = extension.javaVersion.getOrNull()
        if (jv) antArgs.put('javaVersion', jv)

        String preLang = extension.preLanguage.getOrNull()
        if (preLang) antArgs.put('preLanguage', preLang)

        def overviewText = gdoc.overviewText
        if (overviewText != null) {
            File overviewTmp = new File(
                    project.layout.buildDirectory.get().asFile,
                    "tmp/${gdoc.name}/overview.html"
            )
            overviewTmp.parentFile.mkdirs()
            overviewTmp.text = overviewText.asString()
            antArgs.put('overview', overviewTmp.absolutePath)
        }

        def links = gdoc.links ?: []
        def extraStylesheets = extension.additionalStylesheets.files

        project.ant.groovydoc(antArgs) {
            links.each { l ->
                link(packages: (l.packages ?: []).join(','), href: l.url ?: '')
            }
            extraStylesheets.each { f ->
                addStylesheet(file: f.absolutePath)
            }
        }
    }

    @CompileDynamic
    private static List<File> resolveSourceDirectories(Groovydoc gdoc, Project project) {
        def override = gdoc.ext.has('groovydocSourceDirs') ? gdoc.ext.groovydocSourceDirs : null
        if (override != null) {
            Collection<File> files
            if (override instanceof FileCollection) {
                files = ((FileCollection) override).files
            } else if (override instanceof Collection) {
                files = (Collection<File>) override
            } else {
                files = project.files(override).files
            }
            return files.findAll { it.exists() }.unique() as List<File>
        }

        SourceSetContainer sourceSets = project.extensions.findByType(SourceSetContainer)
        if (sourceSets != null) {
            def main = sourceSets.findByName('main')
            if (main != null) {
                List<File> dirs = []
                dirs.addAll(main.groovy.srcDirs.findAll { it.exists() })
                dirs.addAll(main.java.srcDirs.findAll { it.exists() })
                return dirs.unique()
            }
        }
        []
    }

    @CompileDynamic
    private static String resolveAccess(Groovydoc gdoc) {
        def access = gdoc.access
        if (access instanceof Provider) access = ((Provider) access).getOrNull()
        if (access == null) return 'protected'
        access.toString().toLowerCase()
    }

    @CompileDynamic
    private static Object unwrap(Object value, Object defaultValue) {
        if (value instanceof Provider) return ((Provider) value).getOrElse(defaultValue)
        value == null ? defaultValue : value
    }

    @CompileDynamic
    private static File resolveFile(Object value) {
        if (value instanceof Provider) value = ((Provider) value).get()
        if (value instanceof File) return value
        if (value.hasProperty('asFile')) return value.asFile as File
        new File(value.toString())
    }
}
