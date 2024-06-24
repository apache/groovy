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
            conf.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':']) : (v.startsWith('4') ? "org.apache.groovy:groovy:$v" : "org.codehaus.groovy:groovy:$v")))
            if (!v.startsWith('2.4')) {
                conf.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':groovy-dateutil']) : (v.startsWith('4') ? "org.apache.groovy:groovy-dateutil:$v" : "org.codehaus.groovy:groovy-dateutil:$v")))
                conf.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':groovy-datetime']) : (v.startsWith('4') ? "org.apache.groovy:groovy-datetime:$v" : "org.codehaus.groovy:groovy-datetime:$v")))
            }
            conf.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':groovy-swing']) : (v.startsWith('4') ? "org.apache.groovy:groovy-swing:$v" : "org.codehaus.groovy:groovy-swing:$v")))
            conf.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':groovy-sql']) : (v.startsWith('4') ? "org.apache.groovy:groovy-sql:$v" : "org.codehaus.groovy:groovy-sql:$v")))
            conf.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':groovy-xml']) : (v.startsWith('4') ? "org.apache.groovy:groovy-xml:$v" : "org.codehaus.groovy:groovy-xml:$v")))
            conf.dependencies.add(dependencies.create(v == 'current' ? dependencies.project([path: ':groovy-templates']) : (v.startsWith('4') ? "org.apache.groovy:groovy-templates:$v" : "org.codehaus.groovy:groovy-templates:$v")))
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
                'xerces:xercesImpl:2.12.2'
            ].each {conf.dependencies.add(dependencies.create(it)) }
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
