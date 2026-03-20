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

@CompileStatic
final class TargetJavaHomeSupport {

    private TargetJavaHomeSupport() {}

    /** Returns the trimmed value of the {@code target.java.home} Gradle property, or {@code null} if absent/blank. */
    static String targetJavaHome(Project project) {
        if (!project.rootProject.hasProperty('target.java.home')) return null
        String value = project.rootProject.property('target.java.home')?.toString()?.trim()
        return value ?: null
    }

    /** Returns the {@code java} executable path for the given Java home directory. */
    static String javaExecutable(String javaHome) {
        return javaHome ? "${javaHome}/bin/java" : null
    }

    /**
     * Reads the {@code release} file inside the given Java home and returns the
     * feature release number (e.g. 17 for "17.0.2", 21 for "21"), or {@code null}
     * if the file is absent or unparseable.
     */
    static Integer featureVersionFromReleaseFile(String javaHome) {
        if (!javaHome) return null
        File releaseFile = new File(javaHome, 'release')
        if (!releaseFile.exists()) return null
        String line = releaseFile.readLines().find { it.startsWith('JAVA_VERSION=') }
        if (!line) return null
        def matcher = (line =~ /\"(\d+)(?:\.\d+)*\"/)
        return matcher.find() ? (matcher.group(1) as Integer) : null
    }
}

