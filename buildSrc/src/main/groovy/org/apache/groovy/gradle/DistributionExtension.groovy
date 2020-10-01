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
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty

import javax.inject.Inject

@CompileStatic
class DistributionExtension {
    final ListProperty<String> docgeneratorClasses
    final Project project

    CopySpec distSpec
    CopySpec srcSpec
    CopySpec docSpec

    @Inject
    DistributionExtension(ObjectFactory factory, Project project) {
        this.docgeneratorClasses = factory.listProperty(String).convention([])
        this.project = project
    }

    DistributionExtension docs(String p, String... classNames) {
        project.tasks.named('docGDK').configure { DocGDK docgen ->
            docgeneratorClasses.set(docgeneratorClasses.get() +
                    classNames.collect { className ->
                        def src = project.project(p).layout.projectDirectory.file(
                                "src/main/java/${className.replace('.', '/')}.java"
                        ).asFile
                        docgen.inputs.file(src)
                        src.absolutePath
                    }
            )
        }
        this
    }
}
