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
import org.gradle.api.InvalidUserCodeException
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

import javax.inject.Inject

@CompileStatic
class Versions {
    private final Provider<Properties> versions

    @Inject
    Versions(SharedConfiguration sharedConfiguration, ProviderFactory providers, ProjectLayout layout) {
        versions = providers.fileContents(layout.projectDirectory.file("versions.properties"))
                .asText.forUseAtConfigurationTime()
                .map({
                    Properties props = new Properties()
                    props.put("groovy", sharedConfiguration.groovyVersion.get())
                    props.put("groovyBundle", sharedConfiguration.groovyBundleVersion.get())
                    props.load(new StringReader((String) it))
                    props
                }.memoize())
    }

    String getVersion(String key) {
        def v = versions.get().getProperty(key)
        if (v == null) {
            throw new InvalidUserCodeException("No version for $key declared in versions.properties")
        }
        v
    }

    // Make it a bit nicer for use in the Groovy DSL
    Object propertyMissing(String name) {
        getVersion(name)
    }
}
