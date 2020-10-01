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
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.SourceSetContainer

import javax.inject.Inject

@CompileStatic
class CoreExtension {
    final ConfigurableFileCollection classesToBridge
    final SourceSetContainer sourceSets
    final List<String> excludedFromJavadocs = []

    @Inject
    CoreExtension(ObjectFactory objects, SourceSetContainer sourceSets) {
        this.classesToBridge = objects.fileCollection()
        this.sourceSets = sourceSets
    }

    void bridgedClasses(String... classes) {
        classes.each {
            def baseDir = sourceSets.getByName("main").java.outputDir
            classesToBridge.from(new File(baseDir, "/" + it.replace((char) '.', (char) '/') + ".class"))
        }
    }

    void excludeFromJavadoc(String... items) {
        Collections.addAll(excludedFromJavadocs, items)
    }
}
