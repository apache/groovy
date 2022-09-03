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
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.text.SimpleDateFormat

@CompileStatic
class ReleaseInfoGenerator extends DefaultTask {
    @Internal
    String description = "Generates the release info properties file"

    @Input
    final Property<String> version = project.objects.property(String).convention(
            project.providers.gradleProperty("groovyVersion")
    )

    @Input
    final Property<String> bundleVersion = project.objects.property(String).convention(
            project.providers.gradleProperty("groovyBundleVersion")
    )

    @Input
    final Property<Date> buildDate  = project.objects.property(Date).convention(
            project.rootProject.extensions.getByType(SharedConfiguration).buildDate
    )

    @OutputFile
    final RegularFileProperty outputFile = project.objects.fileProperty().convention(
            project.layout.buildDirectory.file("release-info/groovy-release-info.properties")
    )

    @TaskAction
    void generateDescriptor() {
        String date = new SimpleDateFormat('dd-MMM-yyyy').format(buildDate.get())
        String time = new SimpleDateFormat('hh:mm aa').format(buildDate.get())

        outputFile.get().asFile.withWriter('utf-8') {
            it.write("""#
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
#

ImplementationVersion=${version.get()}
BundleVersion=${bundleVersion.get()}
BuildDate=$date
BuildTime=$time
""")
        }
    }

}