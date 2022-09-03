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

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import java.text.DecimalFormat

class PerformanceTestSummary extends DefaultTask {
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    final ConfigurableFileCollection csvFiles = project.objects.fileCollection()

    @TaskAction
    void summarize() {
        def versions = []
        csvFiles.getFiles().each { dataFile ->
            dataFile.eachLine {
                def split = it.split(';')
                def (version, mean, stdDev) = [split[0], split[1], split[2]]
                mean = Double.valueOf(mean)
                stdDev = Double.valueOf(stdDev)
                def id = version == project.version ? 'current' : version
                versions << [id, mean, stdDev]
            }
        }

        versions = versions.sort { ((List) it)[1] }
        def fastest = ((List) versions[0])[1]
        versions.each { version, mean, stdDev ->
            print "Groovy $version Average ${mean}ms ± ${new DecimalFormat("#.##").format(stdDev)}ms "
            if (mean > fastest) {
                def diff = 100 * (mean - fastest) / fastest
                print "(${new DecimalFormat("#.##").format(diff)}% slower)"
            }
            println()
        }
    }
}
