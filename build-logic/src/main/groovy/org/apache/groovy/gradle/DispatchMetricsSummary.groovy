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

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Turns the artefacts of the dispatch-metrics workload run into a single
 * {@code customSmallerIsBetter} JSON report for the benchmark dashboards.
 * <p>
 * Inputs are the {@code -Xlog:class+load} log written by the forked workload
 * JVM and the workload's own JSON fragment (bytecode-size metrics). The log
 * is reduced to deterministic class-loading counters:
 * <ul>
 *   <li>{@code classes.loaded.total} — every class the JVM loaded to run the
 *       workload (startup + dispatch breadth),</li>
 *   <li>{@code classes.lambdaForms} — spun {@code java.lang.invoke.LambdaForm$*}
 *       classes, the one-time MethodHandle cost of the indy machinery,</li>
 *   <li>{@code classes.hidden} — hidden classes (names carry a {@code /0x}
 *       suffix): LambdaForms plus lambda proxies and other spun adapters,</li>
 *   <li>{@code classes.groovyRuntime} — classes from Groovy's own runtime
 *       packages, the runtime's static footprint.</li>
 * </ul>
 * All values are exact for a given JDK + Groovy build (no timing involved),
 * so unlike the JMH numbers they are directly comparable across different
 * CI runner hardware; the dashboards can use tight alert thresholds.
 */
class DispatchMetricsSummary extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty classLoadLog = project.objects.fileProperty()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty workloadJson = project.objects.fileProperty()

    @OutputFile
    final RegularFileProperty jsonReport = project.objects.fileProperty()
            .convention(project.layout.buildDirectory.file('dispatch-metrics.json'))

    @TaskAction
    void summarize() {
        long total = 0
        long lambdaForms = 0
        long hidden = 0
        long groovyRuntime = 0

        classLoadLog.get().asFile.eachLine { line ->
            // decorators are disabled (:none), so lines look like
            //   java.lang.invoke.LambdaForm$MH/0x0000... source: __JVM_LookupDefineClass__
            int sourceIdx = line.indexOf(' source: ')
            if (sourceIdx < 0) return
            String name = line.substring(0, sourceIdx).trim()
            if (!name || name.contains(' ')) return
            total++
            if (name.startsWith('java.lang.invoke.LambdaForm$')) lambdaForms++
            if (name.contains('/0x')) hidden++
            if (name.startsWith('org.codehaus.groovy.') || name.startsWith('org.apache.groovy.')
                    || name.startsWith('groovy.') || name.startsWith('groovyjarjar')) {
                groovyRuntime++
            }
        }

        def entries = [
            [name: 'classes.loaded.total', unit: 'classes', value: total],
            [name: 'classes.lambdaForms', unit: 'classes', value: lambdaForms],
            [name: 'classes.hidden', unit: 'classes', value: hidden],
            [name: 'classes.groovyRuntime', unit: 'classes', value: groovyRuntime],
        ]
        entries += (List) new JsonSlurper().parse(workloadJson.get().asFile)

        entries.each { println "${it.name}: ${it.value} ${it.unit}" }

        def out = jsonReport.get().asFile
        out.parentFile.mkdirs()
        out.text = JsonOutput.prettyPrint(JsonOutput.toJson(entries))
    }
}
