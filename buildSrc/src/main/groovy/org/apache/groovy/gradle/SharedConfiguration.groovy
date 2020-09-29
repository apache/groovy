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
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

@CompileStatic
class SharedConfiguration {
    final Provider<String> groovyVersion
    final Provider<Boolean> isReleaseVersion
    final Provider<Date> buildDate
    final Provider<String> groovyBundleVersion
    final Provider<String> javacMaxMemory
    final Provider<String> groovycMaxMemory
    final Provider<String> javadocMaxMemory
    final Provider<String> installationDirectory

    SharedConfiguration(ProviderFactory providers) {
        groovyVersion = providers.gradleProperty("groovyVersion").forUseAtConfigurationTime()
        groovyBundleVersion = providers.gradleProperty("groovyBundleVersion").forUseAtConfigurationTime()
        javacMaxMemory = providers.gradleProperty("javacMain_mx").forUseAtConfigurationTime()
        groovycMaxMemory = providers.gradleProperty("groovycMain_mx").forUseAtConfigurationTime()
        javadocMaxMemory = providers.gradleProperty("javaDoc_mx").forUseAtConfigurationTime()
        isReleaseVersion = groovyVersion.map { !it.toLowerCase().contains("snapshot") }
        buildDate = isReleaseVersion.map { it ? new Date() : new Date(0) }
        installationDirectory = providers.gradleProperty("groovy_installPath")
            .orElse(providers.systemProperty("installDirectory"))
    }
}
