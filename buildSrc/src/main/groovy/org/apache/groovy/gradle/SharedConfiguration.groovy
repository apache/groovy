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
import org.gradle.api.file.ProjectLayout
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Nested

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
    final Provider<String> binaryCompatibilityBaselineVersion
    final Provider<Boolean> hasCodeCoverage
    final Provider<String> targetJavaVersion
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
        groovyVersion = providers.gradleProperty("groovyVersion").forUseAtConfigurationTime()
        groovyBundleVersion = providers.gradleProperty("groovyBundleVersion").forUseAtConfigurationTime()
        javacMaxMemory = providers.gradleProperty("javacMain_mx").forUseAtConfigurationTime()
        groovycMaxMemory = providers.gradleProperty("groovycMain_mx").forUseAtConfigurationTime()
        javadocMaxMemory = providers.gradleProperty("javaDoc_mx").forUseAtConfigurationTime()
        isReleaseVersion = groovyVersion.map { !it.toLowerCase().contains("snapshot") }
        buildDate = isReleaseVersion.map { it ? new Date() : new Date(0) }
        installationDirectory = providers.gradleProperty("groovy_installPath")
                .orElse(providers.systemProperty("installDirectory"))
        isRunningOnCI = detectCi(rootProjectDirectory, logger)
        artifactory = new Artifactory(layout, providers, logger)
        signing = new Signing(this, objects, providers)
        binaryCompatibilityBaselineVersion = providers.gradleProperty("binaryCompatibilityBaseline").forUseAtConfigurationTime()
        hasCodeCoverage = providers.gradleProperty("coverage").forUseAtConfigurationTime()
                .map { Boolean.valueOf(it) }
                .orElse(
                        providers.provider { startParameter.taskNames.any { it =~ /jacoco/ } }
                )
                .orElse(false)
        targetJavaVersion = objects.property(String).convention("8")
    }

    private static boolean detectCi(File file, Logger logger) {
        def isCi = file.absolutePath =~ /teamcity|jenkins|hudson|travis/
        logger.lifecycle "Detected ${isCi ? 'Continuous Integration environment' : 'development environment'}"
        isCi
    }

    static class Artifactory {
        final Provider<String> username
        final Provider<String> password
        final Provider<String> context
        final Provider<String> repoKey

        Artifactory(ProjectLayout layout, ProviderFactory providers, Logger logger) {
            def artifactoryProperties = providers.fileContents(layout.projectDirectory.file("artifactory.properties")).asText.forUseAtConfigurationTime().map {
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

        private Provider<String> provider(ProviderFactory providers, Provider<Properties> properties, String propertyName, String gradlePropertyName, String envVarName) {
            return providers.gradleProperty(gradlePropertyName).forUseAtConfigurationTime()
                    .orElse(providers.environmentVariable(envVarName).forUseAtConfigurationTime())
                    .orElse(properties.map { it.getProperty(propertyName) })
        }
    }

    static class Signing {
        private final SharedConfiguration config
        final Property<String> keyId
        final Property<String> secretKeyRingFile
        final Property<String> password
        final Provider<Boolean> useGpgCmd
        final Provider<Boolean> forceSign
        final Provider<Boolean> trySign

        Signing(SharedConfiguration config, ObjectFactory objects, ProviderFactory providers) {
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
                    .forUseAtConfigurationTime().map { Boolean.valueOf(it) }.orElse(false)
            forceSign = providers.gradleProperty("forceSign")
                    .forUseAtConfigurationTime().map { Boolean.valueOf(it) }.orElse(false)
            trySign = providers.gradleProperty("trySign")
                    .forUseAtConfigurationTime().map { Boolean.valueOf(it) }.orElse(false)
            this.config = config
        }

        boolean shouldSign(TaskExecutionGraph taskGraph) {
            trySign.get() || (config.isReleaseVersion.get() &&
                    (taskGraph.hasTask(':artifactoryPublish') || forceSign.get()))
        }

        boolean hasAllKeyDetails() {
            return useGpgCmd.get() ||
                    keyId.present && secretKeyRingFile.present && password.present
        }
    }
}
