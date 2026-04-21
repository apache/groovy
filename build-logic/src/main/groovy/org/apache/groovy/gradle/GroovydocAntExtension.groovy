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

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

/**
 * Configures the {@link GroovydocAntPlugin}. Exposes Ant-task-only Groovydoc
 * options that Gradle's built-in {@code Groovydoc} task type doesn't surface,
 * plus a {@link #useAntBuilder} kill-switch for reverting to Gradle's native
 * execution per project.
 */
@CompileStatic
class GroovydocAntExtension {

    final Property<Boolean> useAntBuilder
    final Property<String> javaVersion
    final Property<Boolean> showInternal
    final Property<Boolean> noIndex
    final Property<Boolean> noDeprecatedList
    final Property<Boolean> noHelp
    final Property<String> syntaxHighlighter
    final Property<String> theme
    final ConfigurableFileCollection additionalStylesheets

    @Inject
    GroovydocAntExtension(ObjectFactory objects) {
        useAntBuilder = objects.property(Boolean).convention(true)
        javaVersion = objects.property(String)
        showInternal = objects.property(Boolean).convention(false)
        noIndex = objects.property(Boolean).convention(false)
        noDeprecatedList = objects.property(Boolean).convention(false)
        noHelp = objects.property(Boolean).convention(false)
        syntaxHighlighter = objects.property(String).convention('none')
        theme = objects.property(String).convention('auto')
        additionalStylesheets = objects.fileCollection()
    }
}
