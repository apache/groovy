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
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer

import javax.inject.Inject

@CompileStatic
class PerformanceTestsExtension {
    private final ObjectFactory objects
    private final TaskContainer tasks
    private final ConfigurationContainer configurations
    private final DependencyHandler dependencies
    private final SourceSetContainer sourceSets
    private final ProjectLayout layout
    private final List<File> testFiles = []

    @Inject
    PerformanceTestsExtension(ObjectFactory objects,
                              TaskContainer tasks,
                              ConfigurationContainer configurations,
                              DependencyHandler dependencies,
                              SourceSetContainer sourceSets,
                              ProjectLayout layout
    ) {
        this.objects = objects
        this.tasks = tasks
        this.configurations = configurations
        this.dependencies = dependencies
        this.sourceSets = sourceSets
        this.layout = layout
    }

    void testFiles(FileCollection files) {
        testFiles.addAll(files.asList())
    }

    void versions(String... versions) {
        versions.each {
            version(it)
        }
    }

    void version(String v) {
        def version = v.replace('.', '_')
        def groovyConf = configurations.create("perfGroovy$version") { Configuration conf ->
            conf.canBeResolved = true
            conf.canBeConsumed = false
            conf.resolutionStrategy {
                disableDependencyVerification()
            }
            conf.extendsFrom(configurations.getByName("stats"))
            conf.attributes {
                it.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
                it.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling, Bundling.EXTERNAL))
                it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.JAR))
                it.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_RUNTIME))
            }
            conf.dependencies.add(dependencies.create(v.startsWith('current') ? dependencies.project([path: ':']) : v.startsWith('3') ? "org.codehaus.groovy:groovy:$v" : "org.apache.groovy:groovy:$v"))
            ['groovy-dateutil', 'groovy-datetime', 'groovy-ant', 'groovy-swing', 'groovy-sql', 'groovy-xml', 'groovy-templates'].each { m ->
                conf.dependencies.add(dependencies.create(v.startsWith('current') ? dependencies.project([path: ":$m"]) : v.startsWith('3') ? "org.codehaus.groovy:$m:$v" : "org.apache.groovy:$m:$v"))
            }
            [
                'org.cyberneko:html:1.9.8',
                'commons-net:commons-net:3.11.1',
                'net.sourceforge.htmlunit:htmlunit:2.70.0',
                'com.sleepycat:je:18.3.12',
                'commons-httpclient:commons-httpclient:3.1',
                'net.sf.jopt-simple:jopt-simple:5.0.4',
                'com.baulsupp.kolja:jcurses:0.9.5.3',
                'org.mnode.mstor:mstor:1.0.3',
                'commons-lang:commons-lang:2.6',
                'dnsjava:dnsjava:3.5.3',
                'net.sourceforge.expectj:expectj:2.0.7',
                'jline:jline:2.14.6',
                'prevayler:prevayler:2.02.005',
                'xerces:xercesImpl:2.12.2',
                'hsqldb:hsqldb:1.8.0.10'
            ].each { conf.dependencies.add(dependencies.create(it)) }
        }
        def outputFile = layout.buildDirectory.file("compilation-stats-${version}.csv")
        def perfTest = tasks.register("performanceTestGroovy${version}", JavaExec) { je ->
            je.group = "Performance tests"
            je.mainClass.set('org.apache.groovy.perf.CompilerPerformanceTest')
            je.classpath(groovyConf, sourceSets.getByName('test').output)
            je.jvmArgs = ['-Xms512m', '-Xmx512m']
            je.outputs.file(outputFile)
            je.doFirst {
                def args = [outputFile.get().toString(), "-cp", groovyConf.asPath]
                args.addAll(testFiles.collect { it.toString() })
                je.setArgs(args)
                println je.args.asList()
            }
        }
        tasks.named("performanceTests", PerformanceTestSummary) { pts ->
            pts.csvFiles.from(perfTest)
        }
    }
}
