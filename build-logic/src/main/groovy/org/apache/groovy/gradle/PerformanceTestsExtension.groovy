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
        def groovyConf = configurations.create("perfGroovy$version") { Configuration it ->
            it.canBeResolved = true
            it.canBeConsumed = false
            it.resolutionStrategy {
                disableDependencyVerification()
            }
            it.extendsFrom(configurations.getByName("stats"))
            it.attributes {
                it.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
                it.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling, Bundling.EXTERNAL))
                it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.JAR))
                it.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_RUNTIME))
            }
            it.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':']) : (v.startsWith('4') ? "org.apache.groovy:groovy:$v" : "org.codehaus.groovy:groovy:$v")))
            if (!v.startsWith('2.4')) {
                it.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':groovy-dateutil']) : (v.startsWith('4') ? "org.apache.groovy:groovy-dateutil:$v" : "org.codehaus.groovy:groovy-dateutil:$v")))
                it.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':groovy-datetime']) : (v.startsWith('4') ? "org.apache.groovy:groovy-datetime:$v" : "org.codehaus.groovy:groovy-datetime:$v")))
            }
            it.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':groovy-swing']) : (v.startsWith('4') ? "org.apache.groovy:groovy-swing:$v" : "org.codehaus.groovy:groovy-swing:$v")))
            it.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':groovy-sql']) : (v.startsWith('4') ? "org.apache.groovy:groovy-sql:$v" : "org.codehaus.groovy:groovy-sql:$v")))
            it.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':groovy-xml']) : (v.startsWith('4') ? "org.apache.groovy:groovy-xml:$v" : "org.codehaus.groovy:groovy-xml:$v")))
            it.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':groovy-templates']) : (v.startsWith('4') ? "org.apache.groovy:groovy-templates:$v" : "org.codehaus.groovy:groovy-templates:$v")))
            it.dependencies.add(dependencies.create('org.cyberneko:html:1.9.8'))
            it.dependencies.add(dependencies.create('commons-net:commons-net:3.9.0'))
            it.dependencies.add(dependencies.create('net.sourceforge.htmlunit:htmlunit:2.69.0'))
            it.dependencies.add(dependencies.create('berkeleydb:je:3.2.76'))
            it.dependencies.add(dependencies.create('commons-httpclient:commons-httpclient:3.1'))
            it.dependencies.add(dependencies.create('net.sf.jopt-simple:jopt-simple:5.0.4'))
            it.dependencies.add(dependencies.create('com.baulsupp.kolja:jcurses:0.9.5.3'))
            it.dependencies.add(dependencies.create('org.mnode.mstor:mstor:1.0.2'))
            it.dependencies.add(dependencies.create('commons-lang:commons-lang:2.6'))
            it.dependencies.add(dependencies.create('dnsjava:dnsjava:3.5.2'))
            it.dependencies.add(dependencies.create('net.sourceforge.expectj:expectj:2.0.7'))
            it.dependencies.add(dependencies.create('jline:jline:2.14.6'))
            it.dependencies.add(dependencies.create('prevayler:prevayler:2.02.005'))
        }
        def outputFile = layout.buildDirectory.file("compilation-stats-${version}.csv")
        def perfTest = tasks.register("performanceTestGroovy${version}", JavaExec) {
            it.group = "Performance tests"
            it.mainClass.set('org.apache.groovy.perf.CompilerPerformanceTest')
            it.classpath(groovyConf, sourceSets.getByName('test').output)
            it.jvmArgs = ['-Xms512m', '-Xmx512m']
            it.outputs.file(outputFile)
            def je = it
            it.doFirst {
                def args = [outputFile.get().toString(), "-cp", groovyConf.asPath]
                args.addAll(testFiles.collect { it.toString() })
                je.setArgs(args)
                println je.args.asList()
            }
        }
        tasks.named("performanceTests", PerformanceTestSummary) {
            it.csvFiles.from(perfTest)
        }
    }
}
