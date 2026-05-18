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
import org.gradle.StartParameter
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Nested

@CompileStatic
class SharedConfiguration {
    private static final List<String> DOCUMENTATION_TASK_NAMES = [
            'asciidocAll',
            'asciidoctor',
            'asciidoctorPdf',
            'doc',
            'docGDK',
            'dist',
            'distBin',
            'distDoc',
            'distSdk',
            'groovydocAll',
            'javadocAll'
    ]
    private static final List<String> APACHE_PUBLISH_TASK_PATHS = [
            ':artifactoryPublish',
            ':publishAllPublicationsToApacheRepository'
    ]

    final Provider<String> groovyVersion
    final Provider<Boolean> isReleaseVersion
    final Provider<Date> buildDate
    final Provider<String> groovyBundleVersion
    final Provider<String> javacMaxMemory
    final Provider<String> groovycMaxMemory
    final Provider<String> javadocMaxMemory
    final Provider<String> installationDirectory
    final Provider<String> binaryCompatibilityBaselineVersion
    final Provider<Boolean> hasCodeCoverage
    final Provider<String> targetJavaVersion
    final Provider<String> groovyTargetBytecodeVersion
    final boolean isDocumentationBuild
    final boolean isRunningOnCI

    @Nested
    final Artifactory artifactory

    @Nested
    final Signing signing

    SharedConfiguration(ObjectFactory objects,
                        ProjectLayout layout,
                        ProviderFactory providers,
                        File rootProjectDirectory,
                        Logger logger,
                        StartParameter startParameter
    ) {
        groovyVersion = providers.gradleProperty("groovyVersion")
        groovyBundleVersion = providers.gradleProperty("groovyBundleVersion")
        javacMaxMemory = providers.gradleProperty("javacMain_mx")
        groovycMaxMemory = providers.gradleProperty("groovycMain_mx")
        javadocMaxMemory = providers.gradleProperty("javaDoc_mx")
        isReleaseVersion = groovyVersion.map { !it.toLowerCase().contains("snapshot") } as Provider<Boolean>
        buildDate = isReleaseVersion.map { it ? new Date() : new Date(0) }
        installationDirectory = providers.gradleProperty("groovy_installPath")
                .orElse(providers.systemProperty("installDirectory"))
        isRunningOnCI = detectCi(rootProjectDirectory, logger)
        artifactory = new Artifactory(layout, providers, logger)
        boolean apachePublishRequested = startParameter.taskNames.any { String taskName ->
            isApachePublishTask(taskName)
        }
        signing = new Signing(this, objects, providers, apachePublishRequested)
        binaryCompatibilityBaselineVersion = providers.gradleProperty("binaryCompatibilityBaseline")
        // Evaluate eagerly at construction time (startParameter is available here) so that
        // no lazy provider captures a StartParameter reference, which Gradle would otherwise
        // need to serialize for the configuration cache.
        boolean hasJacocoTask = startParameter.taskNames.any { String name -> name.contains('jacoco') }
        hasCodeCoverage = providers.gradleProperty("coverage")
                .map { Boolean.valueOf(it) }
                .orElse(hasJacocoTask)
                .orElse(false)
        targetJavaVersion = providers.gradleProperty("targetJavaVersion")
        groovyTargetBytecodeVersion = providers.gradleProperty("groovyTargetBytecodeVersion")
        isDocumentationBuild = startParameter.taskNames.any { String taskName ->
            isDocumentationTask(taskName)
        }
        File javaHome = new File(providers.systemProperty('java.home').get())
        String javaVersion = providers.systemProperty('java.version').get()
        String userdir = providers.systemProperty('user.dir').get()
        logger.lifecycle "Using Java from $javaHome (version ${javaVersion})"
        logger.lifecycle "user.dir: ${userdir}"
    }

    private static boolean detectCi(File file, Logger logger) {
        // home/runner/work is path for Github actions
        def isCi = file.absolutePath =~ $/teamcity|jenkins|hudson|/home/runner/work/|travis/$
        logger.lifecycle "Detected ${isCi ? 'Continuous Integration environment' : 'development environment'}"
        isCi
    }

    private static boolean isDocumentationTask(String taskName) {
        String normalized = taskName.toLowerCase(Locale.ROOT)
        normalized.contains('asciidoc') ||
                normalized.contains('asciidoctor') ||
                normalized.contains('javadoc') ||
                normalized.contains('groovydoc') ||
                normalized.endsWith('doc') ||
                normalized.endsWith(':doc') ||
                normalized.contains('docgdk') ||
                normalized == 'dist' ||
                normalized.startsWith('dist') ||
                normalized.contains(':dist') ||
                matchesTaskAbbreviation(taskName, DOCUMENTATION_TASK_NAMES)
    }

    private static boolean isApachePublishTask(String taskName) {
        String normalized = taskName.trim()
        normalized == 'artifactoryPublish' ||
                normalized.endsWith(':artifactoryPublish') ||
                normalized == 'publishAllPublicationsToApacheRepository' ||
                normalized.endsWith(':publishAllPublicationsToApacheRepository')
    }

    // Gradle keeps the originally requested selector in startParameter.taskNames and expands
    // task abbreviations only later, after configuration. Recognize camel-case prefixes here
    // so requests like `jA` still opt into the documentation wiring they resolve to.
    private static boolean matchesTaskAbbreviation(String taskName, List<String> candidateTaskNames) {
        String selector = taskSelector(taskName)
        selector && candidateTaskNames.any { String candidateTaskName ->
            matchesTaskSelector(selector, candidateTaskName)
        }
    }

    private static String taskSelector(String taskName) {
        String normalized = taskName.trim()
        int lastSeparator = normalized.lastIndexOf(':')
        lastSeparator >= 0 ? normalized.substring(lastSeparator + 1) : normalized
    }

    private static boolean matchesTaskSelector(String requestedTaskName, String actualTaskName) {
        if (actualTaskName.regionMatches(true, 0, requestedTaskName, 0, requestedTaskName.length())) {
            return true
        }

        List<String> requestedSegments = taskNameSegments(requestedTaskName)
        List<String> actualSegments = taskNameSegments(actualTaskName)
        if (requestedSegments.size() > actualSegments.size()) {
            return false
        }

        for (int i = 0; i < requestedSegments.size(); i += 1) {
            String requestedSegment = requestedSegments.get(i).toLowerCase(Locale.ROOT)
            String actualSegment = actualSegments.get(i).toLowerCase(Locale.ROOT)
            if (!actualSegment.startsWith(requestedSegment)) {
                return false
            }
        }
        true
    }

    private static List<String> taskNameSegments(String taskName) {
        List<String> segments = []
        StringBuilder currentSegment = new StringBuilder()
        for (int i = 0; i < taskName.length(); i += 1) {
            char current = taskName.charAt(i)
            if (current in ['-', '_', '.']) {
                if (currentSegment.length() > 0) {
                    segments.add(currentSegment.toString())
                    currentSegment.setLength(0)
                }
                continue
            }
            if (currentSegment.length() > 0 && startsNewSegment(taskName, i, currentSegment.charAt(currentSegment.length() - 1))) {
                segments.add(currentSegment.toString())
                currentSegment.setLength(0)
            }
            currentSegment.append(current)
        }
        if (currentSegment.length() > 0) {
            segments.add(currentSegment.toString())
        }
        segments
    }

    private static boolean startsNewSegment(String taskName, int index, char previous) {
        char current = taskName.charAt(index)
        if (!Character.isUpperCase(current)) {
            return false
        }
        if (Character.isLowerCase(previous)) {
            return true
        }
        int nextIndex = index + 1
        nextIndex < taskName.length() && Character.isLowerCase(taskName.charAt(nextIndex))
    }

    static class Artifactory {
        final Provider<String> username
        final Provider<String> password
        final Provider<String> context
        final Provider<String> repoKey

        Artifactory(ProjectLayout layout, ProviderFactory providers, Logger logger) {
            def artifactoryProperties = providers.fileContents(artifactoryFile(providers, layout)).asText.map {
                def props = new Properties()
                props.load(new StringReader(it))
                props
            }
            username = provider(providers, artifactoryProperties, "artifactoryUser", "artifactoryUser", "ARTIFACTORY_USER")
            password = provider(providers, artifactoryProperties, "artifactoryPassword", "artifactoryPassword", "ARTIFACTORY_PASSWORD")
            context = provider(providers, artifactoryProperties, "artifactoryContext", "artifactoryContext", "ARTIFACTORY_CONTEXT")
            repoKey = provider(providers, artifactoryProperties, "artifactoryRepoKey", "artifactoryRepoKey", "ARTIFACTORY_REPO_KEY")
            logger.lifecycle "ArtifactoryUser user: ${username.getOrElse("not defined")}"
        }

        private static Provider<RegularFile> artifactoryFile(ProviderFactory providers, ProjectLayout layout) {
            providers.provider {
                // try to read artifactory.properties
                Directory base = layout.projectDirectory
                RegularFile artifactoryFile = base.file('artifactory.properties')
                while (!artifactoryFile.asFile.exists() && base.asFile.parent) {
                    base = base.dir('..')
                    artifactoryFile = base.file('artifactory.properties')
                }
                artifactoryFile
            }
        }

        private static Provider<String> provider(ProviderFactory providers, Provider<Properties> properties, String propertyName, String gradlePropertyName, String envVarName) {
            return providers.gradleProperty(gradlePropertyName)
                    .orElse(providers.environmentVariable(envVarName))
                    .orElse(properties.map { it.getProperty(propertyName) })
        }
    }

    static class Signing {
        private final SharedConfiguration config
        private final boolean apachePublishRequested
        final Property<String> keyId
        final Property<String> secretKeyRingFile
        final Property<String> password
        final Provider<Boolean> useGpgCmd
        final Provider<Boolean> forceSign
        final Provider<Boolean> trySign

        Signing(SharedConfiguration config, ObjectFactory objects, ProviderFactory providers, boolean apachePublishRequested) {
            keyId = objects.property(String).convention(
                    providers.gradleProperty("signing.keyId")
            )
            secretKeyRingFile = objects.property(String).convention(
                    providers.gradleProperty("signing.secretKeyRingFile")
            )
            password = objects.property(String).convention(
                    providers.gradleProperty("signing.password")
            )
            useGpgCmd = providers.gradleProperty("usegpg")
                    .map { Boolean.valueOf(it) }.orElse(false)
            forceSign = providers.gradleProperty("forceSign")
                    .map { Boolean.valueOf(it) }.orElse(false)
            trySign = providers.gradleProperty("trySign")
                    .map { Boolean.valueOf(it) }.orElse(false)
            this.config = config
            this.apachePublishRequested = apachePublishRequested
        }

        boolean shouldSign() {
            trySign.get() || (config.isReleaseVersion.get() &&
                    (forceSign.get() || apachePublishRequested))
        }

        boolean shouldSign(TaskExecutionGraph taskGraph) {
            trySign.get() || (config.isReleaseVersion.get() &&
                    (forceSign.get() || apachePublishRequested || APACHE_PUBLISH_TASK_PATHS.any {
                        taskGraph.hasTask(it)
                    }))
        }

        boolean hasAllKeyDetails() {
            return useGpgCmd.get() ||
                    keyId.present && secretKeyRingFile.present && password.present
        }
    }
}
