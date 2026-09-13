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
import org.gradle.api.file.RegularFile
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
    final Provider<String> groovyTargetBytecodeVersion
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
        signing = new Signing(this, objects, providers)
        binaryCompatibilityBaselineVersion = providers.gradleProperty("binaryCompatibilityBaseline")
        hasCodeCoverage = providers.gradleProperty("coverage")
                .map { Boolean.valueOf(it) }
                .orElse(
                        providers.provider { startParameter.taskNames.any { it =~ /jacoco/ } }
                )
                .orElse(false)
        targetJavaVersion = providers.gradleProperty("targetJavaVersion")
        groovyTargetBytecodeVersion = providers.gradleProperty("groovyTargetBytecodeVersion")
        File javaHome = new File(providers.systemProperty('java.home').get())
        String javaVersion = providers.systemProperty('java.version').get()
        String userdir = providers.systemProperty('user.dir').get()
        logger.lifecycle "Using Java from $javaHome (version ${javaVersion})"
        logger.lifecycle "user.dir: ${userdir}"
    }

    /**
     * Concurrent {@code Test} tasks admitted by
     * {@code ConcurrentExecutionControlBuildService}. Task-level, not
     * worker-level: peak test JVMs are this times {@link #getTestMaxParallelForks()}.
     * Keep at 2 — raising it without cutting heap/forks overcommits RAM
     * (GHA and a 6-core/23GiB workstation alike).
     */
    static final int TEST_MAX_CONCURRENT_TASKS = 2

    /** Processors the daemon JVM sees; used to size forks and {@code ActiveProcessorCount}. */
    static int availableProcessors() {
        Runtime.runtime.availableProcessors()
    }

    /**
     * {@code maxParallelForks} for {@code Test} tasks.
     * CI stays at 1 (GHA RAM and worker-teardown races). Locally leave ~2
     * CPUs for GC/OS/IDE, then one fork per remaining pair: 6-core → 2,
     * 8-core → 3, 16-core → 7. Combined with {@link #TEST_MAX_CONCURRENT_TASKS}
     * that is 4 workers on a 6-core box rather than 6, which otherwise
     * each size G1/C2 as if they owned all 6 CPUs (load 16–18).
     */
    int getTestMaxParallelForks() {
        if (isRunningOnCI) {
            return 1
        }
        int n = availableProcessors()
        if (n <= 2) {
            return 1
        }
        return Math.max(1, (n - 2).intdiv(2))
    }

    /**
     * Concurrent forked {@code GroovyCompile} workers.
     * CI stays at 2 (1g groovyc overlapping 2g Test OOMs GHA).
     * Locally {@code max(2, nproc/4)} so a 6-core box stays at 2 and a
     * 16-core box can use 4 — do not use nproc/2, which adds fully-sized
     * G1 workers on the same CPUs the Test limiter already occupies.
     */
    int getGroovyCompileMaxConcurrent() {
        if (isRunningOnCI) {
            return 2
        }
        return Math.max(2, availableProcessors().intdiv(4))
    }

    /**
     * {@code -XX:ActiveProcessorCount} for each test / groovyc worker so
     * G1 ({@code ParallelGCThreads}) and C2 ({@code CICompilerCount}) do
     * not assume they own the whole machine. Floor, not ceil: ceil(n/slots)
     * is 2 for every n>=3 and 4 workers then claim 8 CPUs. Floor is 1
     * on a 6-core box (and on JDK 25 that selects Serial GC, which is the
     * right collector when packing several worker JVMs).
     */
    int getTestActiveProcessorCount() {
        int slots = TEST_MAX_CONCURRENT_TASKS * getTestMaxParallelForks()
        if (slots < 1) {
            slots = 1
        }
        return Math.max(1, availableProcessors().intdiv(slots))
    }

    private static boolean detectCi(File file, Logger logger) {
        // Prefer standard CI environment variables. GitHub Actions sets CI=true and
        // GITHUB_ACTIONS=true on every OS; path-only detection historically matched only
        // Linux (/home/runner/work/) and silently treated Windows (D:\a\...) and macOS
        // (/Users/runner/work/...) runners as local dev. That left maxParallelForks at
        // processors/2 on Windows CI, amplifying intermittent Gradle test-worker
        // MessageIOException ("Could not write '/127.0.0.1:…'" / Connection reset by peer)
        // races during worker teardown after suites that had already passed.
        // Keep path heuristics as a fallback for older/self-hosted agents that may not
        // export CI=true (mirrors gradle/build-scans.gradle's env-based isCI).
        Map<String, String> env = System.getenv()
        boolean envCi = 'true'.equalsIgnoreCase(env.get('CI')) ||
                'true'.equalsIgnoreCase(env.get('GITHUB_ACTIONS')) ||
                env.get('TEAMCITY_VERSION') != null ||
                env.get('JENKINS_URL') != null ||
                env.get('HUDSON_URL') != null ||
                'true'.equalsIgnoreCase(env.get('TRAVIS')) ||
                'true'.equalsIgnoreCase(env.get('CIRCLECI')) ||
                'true'.equalsIgnoreCase(env.get('GITLAB_CI'))
        // Path fallbacks: Linux GHA, macOS GHA, Windows GHA (D:\a\repo\repo → D:/a/…), classic CI homes
        String path = file.absolutePath.replace('\\', '/')
        boolean pathCi = path.find(/(?i)(?:teamcity|jenkins|hudson|travis)|\/home\/runner\/work\/|\/Users\/runner\/work\/|[A-Za-z]:\/a\//) != null
        boolean isCi = envCi || pathCi
        logger.lifecycle "Detected ${isCi ? 'Continuous Integration environment' : 'development environment'}" +
                (isCi ? " (via ${envCi ? 'env' : 'path'})" : '')
        isCi
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
                // read artifactory.properties from the repository root only; do not walk
                // ancestor directories, where a parent could inject credentials
                layout.projectDirectory.file('artifactory.properties')
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
                    .map { Boolean.valueOf(it) }.orElse(false)
            forceSign = providers.gradleProperty("forceSign")
                    .map { Boolean.valueOf(it) }.orElse(false)
            trySign = providers.gradleProperty("trySign")
                    .map { Boolean.valueOf(it) }.orElse(false)
            this.config = config
        }

        boolean shouldSign(TaskExecutionGraph taskGraph) {
            trySign.get() || (config.isReleaseVersion.get() &&
                    (forceSign.get() || [':artifactoryPublish', ':publishAllPublicationsToApacheRepository'].any {
                        taskGraph.hasTask(it)
                    }))
        }

        boolean hasAllKeyDetails() {
            return useGpgCmd.get() ||
                    keyId.present && secretKeyRingFile.present && password.present
        }
    }
}
